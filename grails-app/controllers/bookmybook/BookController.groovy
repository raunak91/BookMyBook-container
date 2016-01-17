package bookmybook

import grails.converters.JSON

class BookController {

    def bookService

    def index() { }

    /*
    Inputs Author, Book Name, and City.
    Return a list of user Id and book Id
     */
    def findBooks() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        String author = params.AUTHOR
        String name = params.NAME
        String city = params.CITY
        String availability = params.AVAILABILITY?:''

        if(!city || (!author && !name)) {
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        //TODO :Modify UserBookList
        def userBookList = bookService.fetchBooks(author, name, city, 'true'.equals(availability));

        returnMap = [userBookList: userBookList, status: "SUCCESS"]
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def saveBook() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        String author = params.AUTHOR
        String name = params.NAME

        if(!author || !name) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def book = Book.findByNameAndAuthor(name, author)

        if(book) {
            returnMap.value = "Book Already Present"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else {
            new Book(name: name, author: author, count: 0).save(flush: true, failOnError: true)
        }

        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def removeBook() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        String bookName = params.BOOK_NAME
        String bookAuthor = params.BOOK_AUTHOR

        if(!bookAuthor || !bookName) {
            returnMap.value = "Book Author or Name empty"
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        def book = Book.findByNameAndAuthor(bookName, bookAuthor)

        if(!book) {
            returnMap.value = "Book cannot be determined. Please provide unique identifying details "
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        def bookOwned = UserBookOwnMapping.findByBookId(book.id)

        if(bookOwned) {
            returnMap.value = "Book Owned by User. Cannot be removed."
            return render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
        }

        book.delete(flush: true, failOnError: true)
        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");

    }
}
