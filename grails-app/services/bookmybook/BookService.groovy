package bookmybook

import grails.transaction.Transactional

@Transactional
class BookService {

    def serviceMethod() {

    }

    def fetchBooks(author, name, city) {
        def authorLike = "%" + author + "%"
        def nameLike = "%" + name + "%"

        def users = User.createCriteria().list{
            'books' {
                'like'('name', nameLike)
                'like'('author', authorLike)
            }
            'eq'('city', city)
        }

        def userBookList = users.collect{
            def books = it.books
            def relevantBooks = books.findAll{it.name.contains(name) && it.author.contains(author)}
            new Expando(userName: it.name, userEmail: it.email, books: relevantBooks)
        }
        return userBookList
    }

    def fetchAvailableBooks(author, name, city) {
        def authorLike = "%" + author + "%"
        def nameLike = "%" + name + "%"

        def users = User.createCriteria().list{
            'books' {
                'like'('name', nameLike)
                'like'('author', authorLike)
            }
            'eq'('city', city)
            'ge'('count', 1)
        }

        def userBookList = users.collect{
            def books = it.books
            def relevantBooks = books.findAll{it.name.contains(name) && it.author.contains(author)}
            new Expando(userName: it.name, userEmail: it.email, books: relevantBooks)
        }
        return userBookList
    }
}
