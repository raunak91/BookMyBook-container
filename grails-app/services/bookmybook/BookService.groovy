package bookmybook

import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class BookService {

    def serviceMethod() {

    }

    def addBook(String bookName, String bookAuthor) {
        def book = Book.findByNameAndAuthor(bookName, bookAuthor)

        if(book) {
            returnMap.value = "Book already registered"
        } else {
            book = new Book(name: bookName, author: bookAuthor).save(flush: true, failOnError: true)
            returnMap.status = "SUCCESS"
        }

        returnMap.book = book
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def removeBook(String bookName, String bookAuthor) {
        def returnMap = [:]
        returnMap.status="FAILURE"

        if(!bookAuthor || !bookName) {
            returnMap.value = "Book Author or Name empty"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def book = Book.findByNameAndAuthor(bookName, bookAuthor)

        if(!book) {
            returnMap.value = "Book cannot be determined. Please provide unique identifying details "
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def bookOwned = UserBookOwnMapping.findByBookId(book.id)

        if(bookOwned) {
            returnMap.value = "Book Owned by User. Cannot be removed."
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        book.delete(flush: true, failOnError: true)
        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def fetchBooks(String author, String name, String city) {
        return fetchBooks(author, name, city, false)
    }

    def fetchBooks(String author, String name, String city, Boolean availabilityCheck) {
        def authorLike = "%" + author + "%"
        def nameLike = "%" + name + "%"

        def userBookList = fetchAllBooksByAuthorAndNameAndAvailability(authorLike, nameLike, availabilityCheck)
        List<Long> userList = userBookList.collect {it.userId}
        def validUserBasisCity = User.findAllByCityAndIdInList(city, userList)

        return userBookList.findAll {validUserBasisCity.contains(it.userId)}
    }

    def fetchAllBooksByAuthorAndNameAndAvailability(String author, String name, Boolean availabilityCheck) {
        def bookList = Book.findAllByAuthorAndName(author, name)
        if(availabilityCheck) {
            return UserBookOwnMapping.findAllByBookIdInListAndOpenToLend(bookList.collect {it.id}, true)
        }
        return UserBookOwnMapping.findAllByBookIdInList(bookList.collect {it.id})
    }
}
