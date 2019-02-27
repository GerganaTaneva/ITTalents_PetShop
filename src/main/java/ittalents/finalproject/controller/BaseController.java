package ittalents.finalproject.controller;

import ittalents.finalproject.exceptions.*;
import ittalents.finalproject.model.pojos.ErrorMsg;
import ittalents.finalproject.model.pojos.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@RestController
public abstract class BaseController {

    @ExceptionHandler({ProductNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMsg productNotFound(Exception e) {
        return new ErrorMsg(e.getMessage(), LocalDateTime.now(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler({NotLoggedInException.class, NotLoggedAdminException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorMsg notLoggedHandler(Exception e){
        return new ErrorMsg(e.getMessage(), LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value());
    }

    @ExceptionHandler({BaseException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMsg basedHandler(){
        return new ErrorMsg(new BaseException("You are trying to input not valid properties. Try again!").getMessage(),
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMsg allExceptionHandler(){
        return new ErrorMsg(new Exception("Sorry, the server is temporary down. ").getMessage(), LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }


    protected void validateLogin(HttpSession session) throws NotLoggedInException{
        if(session.getAttribute("loggedUser") == null) {
            throw new NotLoggedInException();
        }
    }

    protected void validateLoginAdmin(HttpSession session)throws BaseException {
        if(session.getAttribute("loggedUser") == null){
            throw new NotLoggedInException();
        }
        else {
            User logged = (User)(session.getAttribute("loggedUser"));
            if(!logged.isAdministrator()) {
                throw new NotLoggedAdminException("Sorry, you are not logged as admin.");
            }
        }
    }

    protected void validateProductInput(String name, String category, double price, int quantity, String manifacturer, String description,
                                String photo)throws InvalidInputException {
        if(name == null || category == null || price < 0 || quantity < 0 || manifacturer == null || description == null || photo == null){
            throw new InvalidInputException();
        }
    }

    public static void validatePetInput(String gender, int age, String breed,
                                         String subBreed, String description, Boolean inSale, int quantity,
                                         double price)throws InvalidInputException{
        if(gender == null || age < 0 || breed == null || subBreed == null || description == null || quantity < 1 || price < 0){
            throw new InvalidInputException();
        }
    }
}
