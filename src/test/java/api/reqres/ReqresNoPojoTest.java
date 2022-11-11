package api.reqres;

import api.reqres.spec.Specifications;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ReqresNoPojoTest {

    private final static String URL = "https://reqres.in";

    /**
     * 1. Получить список пользователей со второй страницы сайта https://reqres.in;
     * 2. Убедиться, что id пользователей содержатся в их avatar;
     * 3. Удебиться, что email пользователей имеет окончание reqres.in;
     */

    @Test
    public void checkAvatarsNoPojoTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL),Specifications.responseSpecOK200());
        Response response = given()
                .when()
                .get("api/users?page=2")
                .then().log().all()
                .body("page", equalTo(2))
                .body("data.id", notNullValue())
                .body("data.email", notNullValue())
                .body("data.first_name", notNullValue())
                .body("data.last_name", notNullValue())
                .body("data.avatar", notNullValue())
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        List<String> emails = jsonPath.get("data.email");
        List<Integer> ids = jsonPath.get("data.id");
        List<String> avatars = jsonPath.get("data.avatar");

        for (int i = 0; i < avatars.size(); i++){
            Assert.assertTrue(avatars.get(i).contains(ids.get(i).toString()));
        }

        Assert.assertTrue(emails.stream().allMatch((x->x.endsWith("reqres.in"))));

    }

    @Test
    public void successUserRegNoPojoTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        Map<String, String> user = new HashMap<>();
        user.put("email", "eve.holt@reqres.in");
        user.put("password", "pistol");
        Response response = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        int id = jsonPath.get("id");
        String token = jsonPath.get("token");
        Assert.assertEquals(4, id);
        Assert.assertEquals("QpwL5tke4Pnpja7X4", token);
    }

    @Test
    public void unsuccessUserRegNoPojoTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecError400());
        Map<String,String> user = new HashMap<>();
        user.put("email", "sydney@fife");
        given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .body("error", equalTo("Missing password"));
    }

    @Test
    public void unsuccessUserRegNoPojoTest2(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecError400());
        Map<String,String> user = new HashMap<>();
        user.put("email", "sydney@fife");
        Response response = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().response();
        JsonPath jsonPath = response.jsonPath();
        String error = jsonPath.get("error");
        Assert.assertEquals("Missing password", error);
    }
}
