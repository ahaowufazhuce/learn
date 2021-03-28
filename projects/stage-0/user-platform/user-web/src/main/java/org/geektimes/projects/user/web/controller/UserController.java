package org.geektimes.projects.user.web.controller;

import org.geektimes.context.ClassicComponentContext;
import org.geektimes.context.ComponentContext;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserService;
import org.geektimes.web.mvc.controller.PageController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;

/**
 * UserController
 *
 * @author liuhao
 */
@Path("/user")
public class UserController implements PageController {
    @Resource(name = "bean/UserService")
    private UserService userService;

    @Override
    @Path("/register")
    public String execute(HttpServletRequest request, HttpServletResponse response) {
        userService = ClassicComponentContext.getInstance().getComponent("bean/UserService");
        User user = new User();
        user.setName(request.getParameter("name"));
        user.setEmail(request.getParameter("email"));
        user.setPassword(request.getParameter("password"));
        user.setPhoneNumber(request.getParameter("phoneNumber"));
        userService.register(user);
        return "success.jsp";
    }
}
