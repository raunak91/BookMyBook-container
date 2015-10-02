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
                'book' {
                    'like'('name', nameLike)
                    'like'('author', authorLike)
                }
            }
            'eq'('city', city)
        }

        return users.collect{
            def relevantBooks = it.books.findAll{name.equals(it.book.name) && author.equals(it.book.author)}.collect{it.book}
            new Expando(userID: it.id, userName: it.name, userEmail: it.email, books: relevantBooks)
        }
    }

    def fetchAvailableBooks(author, name, city) {
        def authorLike = "%" + author + "%"
        def nameLike = "%" + name + "%"

        def users = User.createCriteria().list{
            'books' {
                'book' {
                    'like'('name', nameLike)
                    'like'('author', authorLike)
                }
            }
            'eq'('city', city)
            'ge'('count', 1)
        }

        return users.collect{
            def books = it.books.collect{it.book}
            def relevantBook = books.find{name.equals(it.name) && autho.equals(it.author)}
            return new Expando(userId: it.id, userName: it.name, userEmail: it.email, book: relevantBook)
        }
    }
}
