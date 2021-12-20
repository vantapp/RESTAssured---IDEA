import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test
import static io.restassured.RestAssured.*

class Task extends Base2{
    //14
    @Test
    void newTaskWithoutDescription() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/user/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        File emptyDescriptionFile =new File(getClass().getResource("/task/emptyDescription.json").toURI())
        Response newTaskResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .body(emptyDescriptionFile)
                        .when()
                        .post("/task");

        String error = newTaskResponse.getBody().asString()
        Assert.assertEquals(error,"\"Task validation failed: description: Path `description` is required.\"")
        Assert.assertEquals(newTaskResponse.getStatusCode(),400)

    }
    //15
    @Test
    void newTask() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/user/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        String description = "AntonyTask"
        Response newTaskResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .body("{\"description\":\""+description+"\"}")
                        .when()
                        .post("/task");

        String success = newTaskResponse.jsonPath().getString("success")
        String completed = newTaskResponse.jsonPath().getString("data.completed")
        String descriptionResponse = newTaskResponse.jsonPath().getString("data.description")

        Assert.assertEquals(newTaskResponse.getStatusCode(),201)
        Assert.assertEquals(success, "true")
        Assert.assertEquals(completed, "false")
        Assert.assertEquals(descriptionResponse, description)
    }
    //16
    @Test
    void listTasks() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/user/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        Response listTasksResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .when()
                        .get("/task");


        Assert.assertEquals(listTasksResponse.getStatusCode(),200)

    }
    //17
    @Test
    void updateTasks() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/user/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        String id = "61bfbbb1f244910017889bab"

        File updateTasksFile = new File(getClass().getResource("/task/completedTask.json").toURI())
        Response updateTasksResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .body(updateTasksFile)
                        .when()
                        .put("/task/"+id);

        String completed = updateTasksResponse.jsonPath().getString("data.completed")
        Assert.assertEquals(updateTasksResponse.getStatusCode(),200)
        Assert.assertEquals(completed,"true")
    }
    //18
    @Test
    void listTaskPagination() {
        File credentialNewUserFile = new File(getClass().getResource("/user/credentialNewUser.json").toURI())

        Response credentialNewUserResponse =
                given()
                        .body(credentialNewUserFile)
                        .when()
                        .post("/user/login");

        String token = credentialNewUserResponse.jsonPath().getString("token")

        int limit = 2
        int skip = 0

        Response listTaskPaginationResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .param("limit",limit)
                        .param("skip",skip)
                        .when()
                        .get("/task");

        Integer count = listTaskPaginationResponse.jsonPath().getString("count").toInteger()

        Assert.assertEquals(listTaskPaginationResponse.getStatusCode(),200)
        Assert.assertTrue(count<=limit)
    }
    //19
    @Test
    void deleteTask() {

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MTZjMTQ0ZmFmOTA4NzAwMTdjZTU3YjUiLCJpYXQiOjE2NDAwMTMzNDl9.oCmADqIU8TdaVd2a4iWwanj0zdhBmefpkiQ9EugDR80"
        String id = "619b1e76234bbd0017d92dca"

        Response deleteTaskResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .when()
                        .delete("/task/"+id);

        String success = deleteTaskResponse.jsonPath().getString("success")
        Assert.assertEquals(deleteTaskResponse.getStatusCode(),200)
        Assert.assertEquals(success,"true")
    }
    //20
    @Test
    void listTaskAfterDelete() {

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2MTZjMTQ0ZmFmOTA4NzAwMTdjZTU3YjUiLCJpYXQiOjE2NDAwMTMzNDl9.oCmADqIU8TdaVd2a4iWwanj0zdhBmefpkiQ9EugDR80"
        String id = "619b2635234bbd0017d92ddf"

        Response deleteTaskResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .when()
                        .delete("/task/"+id);

        Response listTasksResponse =
                given()
                        .header("Authorization", "Bearer "+token)
                        .header("Content-Type","application/json")
                        .when()
                        .get("/task");

        ArrayList<String> ids = listTasksResponse.jsonPath().get("data.id")
        boolean exist = ids.contains(id)

        Assert.assertEquals(deleteTaskResponse.getStatusCode(),200)
        Assert.assertEquals(listTasksResponse.getStatusCode(),200)
        Assert.assertFalse(exist)
    }
}
