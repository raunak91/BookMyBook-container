package bookmybook

import grails.converters.JSON

class BookController {

    def bookService

    def index() { }

    def findBooks() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        def author = params.AUTHOR
        def name = params.NAME
        def city = params.CITY

        if(!city || (!author && !name)) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def userBookList = bookService.fetchBooks(author, name, city);

        returnMap = [userBookList: userBookList, status: "SUCCESS"]
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def fetchAvailableBooks() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        def author = params.AUTHOR
        def name = params.NAME
        def city = params.CITY

        if(!city || (!author && !name)) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def userBookList = bookService.fetchAvailableBooks(author, name, city);

        returnMap = [userBookList: userBookList, status: "SUCCESS"]
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def saveBook() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        def author = params.AUTHOR
        def name = params.NAME

        if(!author || !name) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def book = Book.findByNameAndAuthor(name, author)

        if(book) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else {
            new Book(name: name, author: author).save(flush: true, failOnError: true)
        }

        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }
}
