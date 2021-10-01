package controller;

import DAO.IUserDAO;
import DAO.UserDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.User;

@WebServlet(name = "UserServlet", value = "/users")
public class UserServlet extends HttpServlet {

  public static final long serialVersionUID = 1L;
  private IUserDAO iUserDAO;

  @Override
  public void init() {
    iUserDAO = new UserDAO();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
      action = "";
    }
    try {
      switch (action) {
        case "create":
          insertUsert(request, response);
          break;
        case "edit":
          updateUser(request, response);
          break;
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
      action = "";
    }
    try {
      switch (action) {
        case "create":
          insertUsert(request, response);
          break;
        case "edit":
          updateUser(request, response);
          break;
        case "delete":
          deleteUser(request, response);
          break;
        case "permision":
          adduserPermision(request, response);
          break;
        default:
          listUser(request, response);
          break;
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }
  }

  private void adduserPermision(HttpServletRequest request, HttpServletResponse response) {
    User user = new User("hanh", "hanh.nguyen@gmail.com.vn", "vn");
    int[] permision = {1, 2, 4};
    iUserDAO.addUserTransaction(user, permision);
  }

  private void listUser(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    List<User> listUser = iUserDAO.selectAllUsers();
    request.setAttribute("listUser", listUser);
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/list.jsp");
    dispatcher.forward(request, response);
  }

  public void showNewForm(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/create.jsp");
    dispatcher.forward(request, response);
  }

  private void showEditForm(HttpServletRequest request, HttpServletResponse response)
      throws SQLException, ServletException, IOException {
    int id = Integer.parseInt(request.getParameter("id"));
//    User existingUser = iUserDAO.selectUser(id);
    User existingUser = iUserDAO.getUserById(id);
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/edit.jsp");
    request.setAttribute("user", existingUser);
    dispatcher.forward(request, response);
  }

  private void insertUsert(HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, ServletException {
    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String country = request.getParameter("country");
    User newUser = new User(name, email, country);
//    iUserDAO.insertUser(newUser);
    iUserDAO.insertUserStore(newUser);
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/");
    dispatcher.forward(request, response);
  }


  private void updateUser(HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, ServletException {
    int id = Integer.parseInt(request.getParameter("id"));
    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String country = request.getParameter("country");

    User book = new User(id, name, email, country);
    iUserDAO.updateUser(book);
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/edit.jsp");
    dispatcher.forward(request, response);
  }

  private void deleteUser(HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, ServletException {
    int id = Integer.parseInt(request.getParameter("id"));
    iUserDAO.deleteUser(id);

    List<User> listUser = iUserDAO.selectAllUsers();
    request.setAttribute("listUser", listUser);
    RequestDispatcher dispatcher = request.getRequestDispatcher("user/list.jsp");
    dispatcher.forward(request, response);
  }
}
