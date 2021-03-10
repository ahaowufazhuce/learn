package org.geektimes.projects.user.web.listener;

import org.geektimes.context.ComponentContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 监听ServletContext生命周期的变化事件
 * {@link ComponentContext} 初始化器
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
        ComponentContext context = new ComponentContext();
        context.init(servletContext);
    }

    /**
     * // todo by liuhao
     * 只读，不允许关闭，为什么？
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        ComponentContext context = ComponentContext.getInstance();
//        context.destroy();
    }

}
