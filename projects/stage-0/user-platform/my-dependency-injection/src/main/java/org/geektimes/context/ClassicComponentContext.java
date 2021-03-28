package org.geektimes.context;

import org.geektimes.function.ThrowableAction;
import org.geektimes.function.ThrowableFunction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.*;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java传统组件上下文 基于 JNDI 实现
 * 假设一个 Tomcat JVM 进程，三个 Web Apps，会不会相互冲突？（不会冲突）
 * static 字段是 JVM 缓存吗？（是 ClassLoader 缓存）
 *
 * @author Ma
 */
public class ClassicComponentContext implements ComponentContext {

    /**
     * 组件上下文的名字，用于在servlet上下文中传递
     */
    private static final String CONTEXT_NAME = ClassicComponentContext.class.getName();

    /**
     * Java jndi 根目录
     */
    private static final String COMPONENT_ENV_CONTEXT_NAME = "java:comp/env";

    /**
     * 日志
     */
    private static final Logger logger = Logger.getLogger(CONTEXT_NAME);

    /**
     * servlet上下文
     */
    private static ServletContext servletContext;

    /**
     * 使用 Java jndi 根目录 查到到的 Java环境根上下文
     */
    private Context envContext;

    /**
     * 与当前servlet上下文对应的ClassLoader
     */
    private ClassLoader classLoader;

    /**
     * 组件bean生成之后的缓存，相当于bean的池子
     * Key为名称，Value为组件bean
     */
    private Map<String, Object> componentsCache = new LinkedHashMap<>();

    /**
     * 方法缓存，Key 为标注方法，Value 为方法所属对象
     */
    private Map<Method, Object> preDestroyMethodCache = new LinkedHashMap<>();

    /**
     * 获取 ComponentContext 实例
     * 这个实例里，包含了所有已经初始化完成的bean
     * 调用者一般用来查找、关闭
     *
     * @return
     */
    public static ClassicComponentContext getInstance() {
        return (ClassicComponentContext) servletContext.getAttribute(CONTEXT_NAME);
    }

    /**
     * 总的初始化方法
     * 把ComponentContext存入ServletContext的属性中
     *
     * @param servletContext
     * @throws RuntimeException
     */
    public void init(ServletContext servletContext) throws RuntimeException {
        ClassicComponentContext.servletContext = servletContext;
        servletContext.setAttribute(CONTEXT_NAME, this);
        this.init();
    }

    /**
     * 具体初始化方法
     */
    @Override
    public void init() {
        this.initClassLoader();
        this.initEnvContext();
        this.instantiateComponents();
        this.initializeComponents();
        this.registerShutdownHook();
    }

    /**
     * 获取当前ServletContext（WebApp）ClassLoader
     * 设置到组件上下文（ClassicComponentContext）属性中
     */
    private void initClassLoader() {
        this.classLoader = servletContext.getClassLoader();
    }

    /**
     * 初始化根环境envContext
     * 就是使用 Java jndi 根目录 查到到的 Java环境根上下文
     * {@link InitialContext}
     *
     * @throws RuntimeException
     */
    private void initEnvContext() throws RuntimeException {
        if (this.envContext != null) {
            return;
        }
        Context context = null;
        try {
            context = new InitialContext();
            this.envContext = (Context) context.lookup(COMPONENT_ENV_CONTEXT_NAME);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            close(context);
        }
    }

    /**
     * 实例化组件
     * 1.遍历获取所有的组件名称
     * 2.循环组件名称
     * -2.1.通过组件名称查找组件，实际通过envContext的lookup方法查找
     * -2.2.将组件放入componentsMap
     */
    private void instantiateComponents() {
        List<String> componentNames = this.listAllComponentNames();
        // 通过依赖查找，实例化对象（ Tomcat BeanFactory setter 方法的执行，仅支持简单类型）
        componentNames.forEach(name -> {
            logger.info("lookupComponent name : " + name);
            componentsCache.put(name, this.lookupComponent(name));
        });
    }

    /**
     * 初始化组件（支持 Java 标准 Commons Annotation 生命周期）
     * 初始化组件，给组件里的@Resource属性赋值
     * <ol>
     *  <li>注入阶段 - {@link Resource}</li>
     *  <li>初始阶段 - {@link PostConstruct}</li>
     *  <li>销毁阶段 - {@link PreDestroy}</li>
     * </ol>
     */
    private void initializeComponents() {
        componentsCache.values().forEach(this::initializeComponent);
    }

    /**
     * 初始化组件（支持 Java 标准 Commons Annotation 生命周期）
     * 初始化组件，给组件里的@Resource属性赋值
     * <ol>
     *  <li>注入阶段 - {@link Resource}</li>
     *  <li>初始阶段 - {@link PostConstruct}</li>
     *  <li>销毁阶段 - {@link PreDestroy}</li>
     * </ol>
     */
    public void initializeComponent(Object component) {
        Class<?> componentClass = component.getClass();
        // 注入阶段 - {@link Resource}
        this.injectComponent(component, componentClass);
        // 查询候选方法
        List<Method> candidateMethods = this.findCandidateMethods(componentClass);
        // 初始阶段 - {@link PostConstruct}
        this.processPostConstruct(component, candidateMethods);
        // 本阶段处理 {@link PreDestroy} 方法元数据
        this.processPreDestroyMetadata(component, candidateMethods);
    }

    /**
     * 获取组件类中的方法作为下两步的候选方法
     * 1.public方法
     * 2.非static
     * 3.无参数
     *
     * @param componentClass 组件类
     * @return non-null
     */
    private List<Method> findCandidateMethods(Class<?> componentClass) {
        return Stream.of(componentClass.getMethods()).filter(method ->
                !Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0)
                .collect(Collectors.toList());
    }

    /**
     * 注册销毁关闭的钩子
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            processPreDestroy();
        }));
    }

    /**
     * 1.获取componentClass所有的申明属性
     * 2.过滤，保留：非静态属性、有Resource注解属性
     * 3.循环每个过滤结果的field
     * -3.1.获取注解name属性值
     * -3.2.使用jndi查找这个component
     * -3.3.绕过accessible权限
     * -3.4.将查得的component设置到这个field的值中
     *
     * @param component
     * @param componentClass
     */
    protected void injectComponent(Object component, Class<?> componentClass) {
        Stream.of(componentClass.getDeclaredFields()).filter(field -> {
            int mods = field.getModifiers();
            return !Modifier.isStatic(mods) && field.isAnnotationPresent(Resource.class);
        }).forEach(field -> {
            Resource resource = field.getAnnotation(Resource.class);
            String resourceName = resource.name();
            Object injectedObject = lookupComponent(resourceName);
            field.setAccessible(true);
            try {
                field.set(component, injectedObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 执行有PostConstruct注解的方法
     * 1.返回所有公有方法
     * 2.过滤，保留：非静态方法、没有参数、有@PostConstruct注解
     * 3.循环每个过滤结果的method：invoke目标方法
     *
     * @param component
     * @param candidateMethods
     */
    private void processPostConstruct(Object component, List<Method> candidateMethods) {
        candidateMethods
                .stream()
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .forEach(method -> {
                    ThrowableAction.execute(() -> method.invoke(component));
                });
    }

    /**
     * 将带有PreDestroy注解的方法放入preDestroyMethodCache
     *
     * @param component
     * @param candidateMethods
     */
    private void processPreDestroyMetadata(Object component, List<Method> candidateMethods) {
        candidateMethods.stream()
                .filter(method -> method.isAnnotationPresent(PreDestroy.class))
                .forEach(method -> {
                    preDestroyMethodCache.put(method, component);
                });
    }

    /**
     * 执行preDestroyMethodCache中的方法
     */
    private void processPreDestroy() {
        for (Method preDestroyMethod : preDestroyMethodCache.keySet()) {
            // 移除集合中的对象，防止重复执行 @PreDestroy 方法
            Object component = preDestroyMethodCache.remove(preDestroyMethod);
            // 执行目标方法
            ThrowableAction.execute(() -> preDestroyMethod.invoke(component));
        }
    }

    /**
     * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
     *
     * @param function ThrowableFunction
     * @param <R>      返回结果类型
     * @return 返回
     * @see ThrowableFunction#apply(Object)
     */
    private <R> R executeInContext(ThrowableFunction<Context, R> function) {
        return this.executeInContext(function, false);
    }

    /**
     * 在 Context 中执行，通过指定 ThrowableFunction 返回计算结果
     *
     * @param function         ThrowableFunction
     * @param ignoredException 是否忽略异常
     * @param <R>              返回结果类型
     * @return 返回
     * @see ThrowableFunction#apply(Object)
     */
    private <R> R executeInContext(ThrowableFunction<Context, R> function, boolean ignoredException) {
        return this.executeInContext(this.envContext, function, ignoredException);
    }

    /**
     * 具体执行方法
     *
     * @param context
     * @param function
     * @param ignoredException
     * @param <R>
     * @return
     */
    private <R> R executeInContext(Context context, ThrowableFunction<Context, R> function,
                                   boolean ignoredException) {
        R result = null;
        try {
            result = ThrowableFunction.execute(context, function);
        } catch (Throwable e) {
            if (ignoredException) {
                logger.warning(e.getMessage());
            } else {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 内部使用的查找方法
     *
     * @param name
     * @param <C>
     * @return
     */
    public <C> C lookupComponent(String name) {
        return this.executeInContext(context -> (C) context.lookup(name));
    }

    /**
     * 外部使用的查找方法
     * 通过名称进行依赖查找
     *
     * @param name
     * @param <C>
     * @return
     */
    @Override
    public <C> C getComponent(String name) {
        return (C) componentsCache.get(name);
    }

    /**
     * 获取所有的组件名称
     *
     * @return
     */
    @Override
    public List<String> getComponentNames() {
        return new ArrayList<>(componentsCache.keySet());
    }

    /**
     * 遍历获取所有的组件名称
     *
     * @return
     */
    private List<String> listAllComponentNames() {
        return listComponentNames("/");
    }

    /**
     * 遍历获得这个目录名称下的所有组件
     *
     * @param name
     * @return 这个目录名称下的所有组件名称
     */
    private List<String> listComponentNames(String name) {
        return executeInContext(context -> {
            NamingEnumeration<NameClassPair> e = executeInContext(context, ctx -> ctx.list(name), true);
            // 目录 - Context
            // 节点 -
            if (e == null) { // 当前 JNDI 名称下没有子节点
                return Collections.emptyList();
            }
            List<String> fullNames = new LinkedList<>();
            while (e.hasMoreElements()) {
                NameClassPair element = e.nextElement();
                String className = element.getClassName();
                Class<?> targetClass = classLoader.loadClass(className);
                if (Context.class.isAssignableFrom(targetClass)) {
                    // 如果当前名称是目录（Context 实现类）的话，递归查找
                    fullNames.addAll(listComponentNames(element.getName()));
                } else {
                    // 否则，当前名称绑定目标类型的话话，添加该名称到集合中
                    String fullName = name.startsWith("/") ?
                            element.getName() : name + "/" + element.getName();
                    fullNames.add(fullName);
                }
            }
            return fullNames;
        });
    }

    /**
     * 销毁方法
     *
     * @throws RuntimeException
     */
    @Override
    public void destroy() throws RuntimeException {
        this.processPreDestroy();
        this.clearCache();
        this.closeEnvContext();
    }

    private void closeEnvContext() {
        close(this.envContext);
    }

    private void clearCache() {
        componentsCache.clear();
        preDestroyMethodCache.clear();
    }

    private static void close(Context context) {
        if (context != null) {
            ThrowableAction.execute(context::close);
        }
    }
}
