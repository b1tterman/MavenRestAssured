package api.reqres;

import api.reqres.colors.Data;
import api.reqres.registration.Register;
import api.reqres.registration.SuccessReg;
import api.reqres.registration.UnSuccessReg;
import api.reqres.spec.Specifications;
import api.reqres.users.UserData;
import api.reqres.users.UserTime;
import api.reqres.users.UserTimeResponse;
import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ReqresPojoTest {

    private final static String URL = "https://reqres.in";

    /**
     * 1. Получить список пользователей со второй страницы сайта https://reqres.in;
     * 2. Убедиться, что id пользователей содержатся в их avatar;
     * 3. Удебиться, что email пользователей имеет окончание reqres.in;
     */

    @Test
    public void checkAvatarAndIdTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        List<UserData> users = given()
                .when()
                .get("/api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);


        users.forEach(x-> Assert.assertTrue(x.getAvatar().contains(x.getId().toString())));
        Assert.assertTrue(users.stream().allMatch(x->x.getEmail().endsWith("@reqres.in")));

        List<String> avatars = users.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = users.stream().map(x->x.getId().toString()).collect(Collectors.toList());
        for(int i = 0; i<avatars.size(); i++){
            Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }

    @Test
    public void succesRegTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);
        Assert.assertNotNull(successReg.getId());
        Assert.assertNotNull(successReg.getToken());
        Assert.assertEquals(id, successReg.getId());
        Assert.assertEquals(token, successReg.getToken());
    }

    @Test
    public void unSuccessRegTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecError400());
        Register user = new Register("sydney@fife", "");
        UnSuccessReg unSuccessReg = given()
                .body(user)
                .when()
                .post("api/register")
                .then().log().all()
                .extract().as(UnSuccessReg.class);
        Assert.assertEquals("Missing password", unSuccessReg.getError());
    }

    @Test
    public void sortedYearsTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        List<Data> colors = given()
                .when()
                .get("api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", Data.class);
        List<Integer> years = colors.stream().map(Data::getYear).collect(Collectors.toList());
        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(sortedYears, years);
        System.out.println(years);
        System.out.println(sortedYears);
    }

    @Test
    public void deleteUserTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecUnique(204));
        given()
                .when()
                .delete("/api/users/2")
                .then().log().all();
    }

    @Test
    public void timeTest(){
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOK200());
        UserTime user = new UserTime("morpheus", "zion resident");
        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);
        String regex = "(.{11})$";
        String regex2 = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex,"");
        Assert.assertEquals(currentTime, response.getUpdatedAt().replaceAll(regex2,""));
        System.out.println("currentTime: " + currentTime);
        System.out.println("updateTime: " + response.getUpdatedAt().replaceAll(regex2,""));
    }
}
