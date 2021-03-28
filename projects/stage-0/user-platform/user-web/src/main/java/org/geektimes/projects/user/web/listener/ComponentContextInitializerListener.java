package org.geektimes.projects.user.web.listener;

import org.geektimes.context.ClassicComponentContext;
import org.geektimes.context.ComponentContext;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.management.UserManager;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.management.ManagementFactory;

/**
 * 监听ServletContext生命周期的变化事件
 * 初始化组件
 * {@link ClassicComponentContext} 初始化器
 * ContextLoaderListener
 */
public class ComponentContextInitializerListener implements ServletContextListener {

    private ServletContext servletContext;

    /**
     * 当ServletContext初始化完成的时候，初始化自己的ComponentContext
     * 1.获取当前事件的ServletContext
     * 2.在ComponentContext.init(ServletContext servletContext)中，把ComponentContext存入ServletContext的属性中
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        ClassicComponentContext context = new ClassicComponentContext();
        context.init(servletContext);
        this.registerMBean();
    }

    /**
     * 注册了一个bean
     * 使用jolokia的read、exec、write方法执行皆可成功
     *
     * @throws Exception
     */
    public void registerMBean() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("org.geektimes.projects.user.management:type=User");
            User user = new User();
            user.setName("刘浩");
            mBeanServer.registerMBean(new UserManager(user), objectName);
        } catch (Exception e) {

        }
    }

    /**
     * // todo by liuhao
     * 只读，不允许关闭，为什么？
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ComponentContext context = ClassicComponentContext.getInstance();
        context.destroy();
    }

}
