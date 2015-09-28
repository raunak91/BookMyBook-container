package bookmybook

import grails.converters.JSON

class UserController {

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
        def books = user?.books?:[]

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
        if(user.books.contains(book)) {
            returnMap.value = "Book Already owned by user"
            render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
            return
        }
        user.books.add(book)
        user.save()

        returnMap.status = "SUCCESS"
        render(text: returnMap as JSON, contentType: "application/json", encoding: "UTF-8");
    }
}
