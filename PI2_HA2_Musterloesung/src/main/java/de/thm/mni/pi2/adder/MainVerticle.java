package de.thm.mni.pi2.adder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    // First, we need to handle CORS
    // we allow ports 63342 and 63343 (WebStorms Live Server)
    // and GET and POST
    router.route().handler(CorsHandler.create()
      .addOrigin("http://localhost:63342")
      .addOrigin("http://localhost:63343")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedHeader("content-type")
      .allowCredentials(true)
    );

    // Initialize session handling for the application using a local session store.
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

    // Enable parsing of request bodies (used for handling JSON in POST request)
    router.route().handler(BodyHandler.create());

    // Define routes for the application, setting up handlers for each specific task.
    // the add route requires a login, which is verified by the middleware-handler (see below)
    router.post("/add").handler(this::middleware).handler(this::add);
    router.post("/cookies").handler(this::createCookies);
    router.get("/cookies").handler(this::getCookies);
    router.post("/login").handler(this::login);

    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  /**
   * Checks if a user is logged in by verifying a username in the session.
   * Proceeds if true, otherwise returns a 401 error.
   *
   * @param routingContext Provides context for handling web requests.
   */
  public void middleware(RoutingContext routingContext) {
    if (routingContext.session().get("username") != null) {
      routingContext.next();
    } else {
      JsonObject jsonResponse = new JsonObject().put("error", "Unauthorized! Please login first.");
      response(routingContext.response(), 401, jsonResponse);
    }
  }

  /**
   * Processes a request to add three integer values (a, b, c) from a JSON payload.
   * Returns the sum or an error message if input is invalid or another error occurs.
   *
   * @param routingContext Provides context for handling web requests.
   */
  private void add(RoutingContext routingContext) {
    try {
      JsonObject jsonObject = routingContext.body().asJsonObject();
      String a = jsonObject.getString("a");
      String b = jsonObject.getString("b");
      String c = jsonObject.getString("c");

      if (a == null || a.isEmpty() || b == null || b.isEmpty() || c == null || c.isEmpty()) {
        throw new IllegalArgumentException();
      }

      int sum = Integer.parseInt(a) + Integer.parseInt(b) + Integer.parseInt(c);
      response(routingContext.response(), 201, new JsonObject().put("sum", sum));
    } catch (DecodeException e) {
      response(routingContext.response(), 400, new JsonObject().put("error", "Invalid JSON input."));
    } catch (NumberFormatException e) {
      response(routingContext.response(), 400, new JsonObject().put("error", "Invalid number format. All values must be valid integers."));
    } catch (IllegalArgumentException e) {
      response(routingContext.response(), 400, new JsonObject().put("error", "Invalid JSON input."));
    } catch (Exception e) {
      response(routingContext.response(), 500, new JsonObject().put("error", "An error has occurred."));
    }
  }

  /**
   * Creates cookies with 'firstName' and 'lastName' from a JSON payload.
   * Returns these names in the response or handles errors if input is invalid or another exception occurs.
   *
   * @param routingContext Provides context for handling web requests.
   */
  private void createCookies(RoutingContext routingContext) {
    try {
      JsonObject jsonObject = routingContext.body().asJsonObject();
      String firstName = jsonObject.getString("firstName");
      String lastName = jsonObject.getString("lastName");

      if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
        throw new IllegalArgumentException();
      }

      // Note: For production use, cookies should include security attributes to prevent XSS and CSRF attacks:
      // - setHttpOnly(true): Prevents JavaScript access to cookies (XSS protection)
      // - setSecure(true): Ensures cookies are only sent over HTTPS (use false for localhost/HTTP)
      // - setSameSite(CookieSameSite.STRICT): Prevents cookies from being sent in cross-site requests (CSRF protection)
      // - setPath("/"): Defines the path where the cookie is valid
      // - setMaxAge(seconds): Sets the cookie expiration time
      // Example: Cookie.cookie("firstName", firstName).setHttpOnly(true).setSecure(false).setSameSite(CookieSameSite.STRICT).setPath("/").setMaxAge(3600)
      routingContext.response()
        .addCookie(Cookie.cookie("firstName", firstName))
        .addCookie(Cookie.cookie("lastName", lastName));
      // Alternative: .putHeader("Set-Cookie","myCookie=firstName; Path=/; Max-Age=3600")
      // Alternative: .putHeader("Set-Cookie","myCookie=lastName; Path=/; Max-Age=3600")

      JsonObject jsonResponse = new JsonObject().put("firstName", firstName).put("lastName", lastName);
      response(routingContext.response(), 201, jsonResponse);
    } catch (DecodeException | IllegalArgumentException e) {
      response(routingContext.response(), 400, new JsonObject().put("error", "Invalid JSON input."));
    } catch (Exception e) {
      response(routingContext.response(), 500, new JsonObject().put("error", "An error has occurred."));
    }
  }

  /**
   * Retrieves 'firstName' and 'lastName' cookies from the request.
   * Returns these values in the response or handles errors if cookies are not found or another exception occurs.
   *
   * @param routingContext Provides context for handling web requests.
   */
  private void getCookies(RoutingContext routingContext) {
    try {
      Cookie firstNameCookie = routingContext.request().getCookie("firstName");
      Cookie lastNameCookie = routingContext.request().getCookie("lastName");

      if (firstNameCookie == null || lastNameCookie == null) {
        throw new IllegalArgumentException();
      }

      JsonObject jsonResponse = new JsonObject()
        .put("firstName", firstNameCookie.getValue())
        .put("lastName", lastNameCookie.getValue());

      response(routingContext.response(), 200, jsonResponse);
    } catch (IllegalArgumentException e) {
      response(routingContext.response(), 400, new JsonObject().put("error", "No Cookies found."));
    } catch (Exception e) {
      response(routingContext.response(), 500, new JsonObject().put("error", "An error has occurred."));
    }
  }


  /**
   * Logs in a user by setting a 'username' provided in the JSON payload into the session.
   * It returns the username in the response or handles errors if the JSON input is invalid or another exception occurs.
   *
   * @param routingContext Provides context for handling web requests.
   */
  private void login(RoutingContext routingContext) {
    try {
      JsonObject jsonObject = routingContext.body().asJsonObject();
      String username = jsonObject.getString("username");

      if (username == null || username.isEmpty()) {
        throw new IllegalArgumentException();
      }
      routingContext.session().put("username", username);
      response(routingContext.response(), 201, new JsonObject().put("username", username));
    } catch (DecodeException | IllegalArgumentException e) {
      response(routingContext.response(), 400, new JsonObject().put("error", "Invalid JSON input."));
    } catch (Exception e) {
      response(routingContext.response(), 500, new JsonObject().put("error", "An error has occurred."));
    }
  }

  /**
   * Sends a response with the specified status code and JSON payload.
   *
   * @param response   The HTTP server response object to be used for sending the response.
   * @param statusCode The HTTP status code to set for the response.
   * @param json       The JSON object containing the data to be sent in the response.
   */
  private void response(HttpServerResponse response, Integer statusCode, JsonObject json) {
    response
      .putHeader("content-type", "application/json")
      .setStatusCode(statusCode)
      .end(Json.encodePrettily(json));
  }

}
