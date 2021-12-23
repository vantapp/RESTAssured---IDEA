import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import static io.restassured.RestAssured.*
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath


class User extends BaseUser {
    //1
    @Test
    void registerExistingEmail() {

        File existingEmailFile = new File(getClass().getResource("/user/existingEmail.json").toURI())

        Map<String,Object> result =
                new ObjectMapper().readValue(new File("src/test/resources/user/existingEmail.json"), HashMap.class);

        String email = result.get("email");

        Response existingEmailResponse =
                given()
                        .body(existingEmailFile)
                        .when()
                        .post("/register");

        String error = existingEmailResponse.getBody().asString()

        Assert.assertEquals(existingEmailResponse.getStatusCode(), 400)
        Assert.assertEquals(error,"\"E11000 duplicate key error collection: todo-list.users index: email_1 dup key: { email: \\\""+email+"\\\" }\"")
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
        Assert.assertEquals(error,"\"User validation failed: email: Email is invalid\"")
    }
    //3
    @Test
    void registerNewUser() {
        File registerNewUserFile = new File(getClass().getResource("/user/newUser.json").toURI())
        Map<String,Object> result =
                new ObjectMapper().readValue(new File("src/test/resources/user/newUser.json"), HashMap.class);

        String age = result.get("age");
        String name = result.get("name");
        String email = result.get("email")
        String emailLowerCase = email.toLowerCase()

        Response registerNewUserResponse =
                given()
                        .body(registerNewUserFile)
                        .when()
                        .post("/register");

        String ageResponse = registerNewUserResponse.jsonPath().getString("user.age")
        String nameResponse = registerNewUserResponse.jsonPath().getString("user.name")
        String emailResponse =registerNewUserResponse.jsonPath().getString("user.email")

        Assert.assertEquals(registerNewUserResponse.getStatusCode(), 201)
        registerNewUserResponse.then().assertThat().body(matchesJsonSchemaInClasspath("user/UserSchema.json"))
        Assert.assertEquals(ageResponse, age)
        Assert.assertEquals(nameResponse, name)
        Assert.assertEquals(emailResponse, emailLowerCase)
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
        Assert.assertEquals(error, "\"Unable to login\"")

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
        Assert.assertEquals(error, "\"Unable to login\"")
    }
    //6
    @Test
    void credentialNewUser() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Map<String,Object> result =
                new ObjectMapper().readValue(new File("src/test/resources/user/credentialNewUser.json"), HashMap.class);

        String email = result.get("email");
        String emailLowerCase = email.toLowerCase()

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/login");

        String emailResponse = credentialNewUserResponse.jsonPath().getString("user.email")

        Assert.assertEquals(credentialNewUserResponse.getStatusCode(), 200)
        credentialNewUserResponse.then().assertThat().body(matchesJsonSchemaInClasspath("user/UserSchema.json"))
        Assert.assertEquals(emailResponse,emailLowerCase)

    }
    //7
    @Test
    void logoutWithoutToken() {

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
        Map<String,Object> result =
                new ObjectMapper().readValue(new File("src/test/resources/user/newUser.json"), HashMap.class);

        String age = result.get("age")
        String name = result.get("name")
        String email = result.get("email")
        String emailLowerCase = email.toLowerCase()

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

        String nameResponse= getUserResponse.jsonPath().getString("name")
        String ageResponse = getUserResponse.jsonPath().getString("age")
        String emailResponse = getUserResponse.jsonPath().getString("email")

        Assert.assertEquals(getUserResponse.getStatusCode(),200)
        Assert.assertEquals(nameResponse,name)
        Assert.assertEquals(ageResponse,age)
        Assert.assertEquals(emailResponse,emailLowerCase)
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
        Response updateSomeDataResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type", "application/json")
                        .body("{\"name\": \""+newName+"\",\"age\":"+newAge+"}")
                        .when()
                        .put("/me")

        String updatedAge= updateSomeDataResponse.jsonPath().getString("data.age")
        String updatedName= updateSomeDataResponse.jsonPath().getString("data.name")

        Assert.assertEquals(updateSomeDataResponse.getStatusCode(),200)
        Assert.assertEquals(updatedAge,newAge)
        Assert.assertEquals(updatedName,newName)
    }

}