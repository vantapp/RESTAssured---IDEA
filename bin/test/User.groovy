import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

import static io.restassured.RestAssured.*

class User extends Base{

    @test
    void registrarCorreoNoExistente(){
        File userFile = new FILE(getClass().getResource("/user.json").toURI())

        Response createResponse =
        given()
                .body(userFile)
                .when()
                .post("/register")

        Assert.assertEquals(userFile.getStatusCode(),201)
    }

    void registrarCorreoExistente(){
        File correoExistenteFile = new FILE(getClass().getResource("/CorreoExistente.json").toURI())

        Response createResponse =
        given()
                .body(userFile)
                .when()
                .post("/register")
        
        Assert.assertEquals(correoExistenteFile.getStatusCode(),400)
    }
