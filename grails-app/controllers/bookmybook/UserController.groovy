package bookmybook

import grails.converters.JSON

class UserController {

    def bookService

    def index() { }

    def saveUser() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def name = params.NAME
        def email = params.EMAIL
        def city = params.CITY

        if (!deviceId || !email) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        if(User.findByDeviceId(deviceId)) {
            returnMap.reason = "DeviceId Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else if(User.findByEmail(email)) {
            returnMap.reason = "Email Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else {
            new User(name: name, email: email, deviceId: deviceId, city: city).save(flush:true, failOnError: true)
        }

        returnMap.status="SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def getMyBooks() {
        def returnMap = [:]
        returnMap.status = "FAILURE"

        def deviceId = params.DEVICE_ID

        def user = User.findByDeviceId(deviceId)
        def books = []
        if(user) {
            books = user.books ? user.books.collect { it.book } : []
        } else {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        returnMap = [status: "SUCCESS", books: books]
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def findUser() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def name = params.NAME
        def email = params.EMAIL
        def city = params.CITY

        if (!deviceId || !email) {
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        if(User.findByDeviceId(deviceId)) {
            returnMap.reason = "DeviceId Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else if(User.findByEmail(email)) {
            returnMap.reason = "Email Registered"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        } else {
            new User(name: name, email: email, deviceId: deviceId, city: city).save(flush:true, failOnError: true)
        }

        returnMap.status="SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def addBook() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def bookName = params.BOOK_NAME
        def bookAuthor = params.BOOK_AUTHOR

        if(!deviceId || !bookAuthor || !bookName) {
            returnMap.value = "Book Author or Name empty"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def book = Book.findByNameAndAuthor(bookName, bookAuthor)

        if(!book) {
            book = new Book(name: bookName, author: bookAuthor).save(flush: true, failOnError: true)
        }

        def user = User.findByDeviceId(deviceId)
        if(!user) {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def bookOwned = user.books.find{it.book == book}

        if(bookOwned) {
            bookOwned.ownedCount ++
            user.books.add(bookOwned)
        } else {
            def userBookOwnMap = new UserBookOwnMapping()

            userBookOwnMap.user = user
            userBookOwnMap.book = book
            userBookOwnMap.ownedCount = 1

            user.books.add(userBookOwnMap)
        }

        user.save(flush: true, failOnError: true)
        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def removeBook() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def bookName = params.BOOK_NAME
        def bookAuthor = params.BOOK_AUTHOR

        if(!deviceId || !bookAuthor || !bookName) {
            returnMap.value = "Book Author or Name empty"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def book = Book.findByNameAndAuthor(bookName, bookAuthor)

        if(!book) {
            returnMap.value = "Book not present"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def user = User.findByDeviceId(deviceId)
        if(!user) {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def bookOwned = user.books.find{it.book == book}

        if(bookOwned) {
            bookOwned.ownedCount --
            user.books.add(bookOwned)
        } else {
            returnMap.value = "Book not Owned by User"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        user.save(flush: true, failOnError: true)
        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    def borrowBook() {
        def returnMap = [:]
        returnMap.status="FAILURE"

        def deviceId = params.DEVICE_ID
        def bookName = params.BOOK_NAME
        def bookAuthor = params.BOOK_AUTHOR
        def city = params.CITY
        def bookOwnerId = params.OWNER

        if(!deviceId || !bookAuthor || !bookName) {
            returnMap.value = "Book Author or Name empty"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }
        def user = User.findByDeviceId(deviceId)
        if(!user) {
            returnMap.value = "USER not found"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }

        def availableBooks = bookService.fetchAvailableBooks(bookAuthor, bookName, city)
        def bookWanted = availableBooks.find{it.userId == bookOwnerId}
        if(bookWanted) {
            lendBook(user, bookWanted)
        } else {
            lendBook(user, availableBooks.get(0))
        }

        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }

    private def lendBook(lendee, book) {
        def lender = User.get(book.userId)
        def userBookLentMapping = new UserBookLentMapping()
        userBookLentMapping.bookLendee = lendee
        userBookLentMapping.bookLender = lender
        userBookLentMapping.book = book.book
        userBookLentMapping.save(flush: true, failOnError: true)
    }
}
