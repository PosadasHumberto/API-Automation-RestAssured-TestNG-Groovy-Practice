import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

class Books extends Base {

    @Test       //validando que existan libros
    void getBooksList() {
        Response response = given()
        .log().all()
        .get("/books");

        List<String> allBooks = response.path("data.title");
        Assert.assertTrue(allBooks.size() > 1, "No books returned");

    }

    @Test       //Validando que el esquema es valido
    void booksSchemaIsValid() {
        given()
        .log().all()
        .get("/books")
        .then()
        .assertThat()
        .body(matchesJsonSchemaInClasspath("booksSchema.json"));    //este esquema debe ser dado por los desarrolladores de la aplicacion para que podamos hacer los tests.
    }

    @Test       //validando la creacion y supresion de un Book
    void createAndDeleteBook() {
        File bookFile = new File(getClass().getResource("book.json").toURI())

        Response createResponse = given()
        .body(bookFile)
        .when()
        .post("/books");

        String responseID = createResponse.jsonPath().getString("post.book_id");

        Response deleteResponse = given()
                .body("{\n" +
                        "\t\"book_id\": " + responseID + "\n" +
                        "}")
                .when()
                .delete("/books")

        Assert.assertEquals(deleteResponse.getStatusCode().intValue(), (int)200);
        Assert.assertEquals(deleteResponse.jsonPath().getString("message"), "Book successfully deleted")

    }

    @Test
    void deleteNonExistingBook_FailMessage() {
        String nonExistentBookID = "456123"

        Response deleteResponse =
                given()
                        .body("{\n" +
                                "\t\"book_id\": " + nonExistentBookID + "\n" +
                                "}")
                        .when()
                        .delete("/books")

        Assert.assertEquals(deleteResponse.getStatusCode().intValue(), (int)500)
        Assert.assertEquals(deleteResponse.jsonPath().getString("error"), "Unable to find book id: " + nonExistentBookID)

    }

}
