import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import static io.restassured.RestAssured.*
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath


class User extends Base {
    //1
    @Test
    void registerExistingEmail() {

        File existingEmailFile = new File(getClass().getResource("/user/existingEmail.json").toURI())


        Response existingEmailResponse =
                given()
                        .body(existingEmailFile)
                        .when()
                        .post("/register");

        String error = existingEmailResponse.getBody().asString()
        Assert.assertEquals(existingEmailResponse.getStatusCode(), 400)
        Assert.assertTrue(error.contains("E11000 duplicate key error collection: todo-list.users index: email_1 dup key"))
    }
    //2
    @Test
    void incorrectEmail(){
        File incorrectEmailFile = new File(getClass().getResource("/user/incorrectEmail.json").toURI())

        Response incorrectEmailResponse =
                given()
                        .body(incorrectEmailFile)
                        .when()
                        .post("/register");

        String error = incorrectEmailResponse.getBody().asString()
        Assert.assertEquals(incorrectEmailResponse.getStatusCode(), 400)
        Assert.assertTrue(error.contains("User validation failed: email: Email is invalid"))
    }
    //3
    @Test
    void registerNewUser() {
        File registerNewUserFile = new File(getClass().getResource("/user/newUser.json").toURI())

        Response registerNewUserResponse =
                given()
                        .body(registerNewUserFile)
                        .when()
                        .post("/register");
        Assert.assertEquals(registerNewUserResponse.getStatusCode(), 201)
        registerNewUserResponse.then().assertThat().body(matchesJsonSchemaInClasspath("user/UserSchema.json"))
    }
    //4
    @Test
    void emptyEmail() {
        File emptyEmailFile = new File(getClass().getResource("/user/emptyEmail.json").toURI())

        Response emptyEmailResponse =
                given()
                        .body(emptyEmailFile)
                        .when()
                        .post("/login");

        String error = emptyEmailResponse.getBody().asString()
        Assert.assertEquals(emptyEmailResponse.getStatusCode(), 400)
        Assert.assertTrue(error.contains("Unable to login"))
    }
    //5
    @Test
    void emptyPassword() {
        File emptyPasswordFile = new File(getClass().getResource("/user/emptyPassword.json").toURI())

        Response emptyPasswordResponse =
                given()
                        .body(emptyPasswordFile)
                        .when()
                        .post("/login");

        String error = emptyPasswordResponse.getBody().asString()
        Assert.assertEquals(emptyPasswordResponse.getStatusCode(), 400)
        Assert.assertTrue(error.contains("Unable to login"))
    }
    //6
    @Test
    void credentialNewUser() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        Assert.assertEquals(credentialNewUserResponse.getStatusCode(), 200)
        credentialNewUserResponse.then().assertThat().body(matchesJsonSchemaInClasspath("user/UserSchema.json"))
    }
    //7
    @Test
    void logoutWithoutToken() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        Response logoutResponse =
                given()
                        .header("Authorization", "")
                        .when()
                        .post("/logout");
        String error = logoutResponse.jsonPath().getString("error")

        Assert.assertEquals(logoutResponse.getStatusCode(),401)
        Assert.assertEquals(error,"Please authenticate.")
    }
    //8
    @Test
    void logout() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        Response logoutResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .when()
                        .post("/logout");
        String success = logoutResponse.jsonPath().getString("success")

        Assert.assertEquals(logoutResponse.getStatusCode(),200)
        Assert.assertEquals(success,"true")
    }
    //9
    @Test
    void logoutRepeat() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

                given()
                        .header("Authorization", "Bearer "+token)
                        .when()
                        .post("/logout");

        Response logoutResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .when()
                        .post("/logout");

        String error = logoutResponse.jsonPath().getString("error")

        Assert.assertEquals(logoutResponse.getStatusCode(),401)
        Assert.assertEquals(error,"Please authenticate.")
    }
    //10
    @Test
    void getUser() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        Response getUserResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .when()
                        .get("/me")

        String name= getUserResponse.jsonPath().getString("name")
        String age= getUserResponse.jsonPath().getString("age")

        Assert.assertEquals(getUserResponse.getStatusCode(),200)
        Assert.assertEquals(name,"Antony")
        Assert.assertEquals(age,"20")
    }
    //11
    @Test
    void updateOneData() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        String newAge = "25"
        Response updateOneDataResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type", "application/json")
                        .body("{\n\t\"age\": "+newAge+"\n}")
                        .when()
                        .put("/me")

        String updatedAge= updateOneDataResponse.jsonPath().getString("data.age")

        Assert.assertEquals(updateOneDataResponse.getStatusCode(),200)
        Assert.assertEquals(updatedAge,newAge)
    }
    //12 y 13
    @Test
    void updateSomeData() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        String newAge = "25"
        String newName = "Juan"
        Response updateOneDataResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type", "application/json")
                        .body("{\"name\": \""+newName+"\",\"age\":"+newAge+"}")
                        .when()
                        .put("/me")

        String updatedAge= updateOneDataResponse.jsonPath().getString("data.age")
        String updatedName= updateOneDataResponse.jsonPath().getString("data.name")

        Assert.assertEquals(updateOneDataResponse.getStatusCode(),200)
        Assert.assertEquals(updatedAge,newAge)
        Assert.assertEquals(updatedName,newName)
    }

}