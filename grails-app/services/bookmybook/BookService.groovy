package bookmybook

import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class BookService {

    def serviceMethod() {

    }

    Book addBook(String bookName, String bookAuthor) {
        def book = Book.findByNameAndAuthor(bookName, bookAuthor)

        if(book) {
            return null
        } else {
            return new Book(name: bookName, author: bookAuthor).save(flush: true, failOnError: true)
        }
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
