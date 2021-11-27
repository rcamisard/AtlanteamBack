package atlanteam.atlanteamserver.Controller;

import atlanteam.atlanteamserver.Service.Connexion.ConnexionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnexionController {

    @Autowired
    ConnexionService connexionService;

    @GetMapping("/connect")
    @ResponseStatus(HttpStatus.OK)
    public String generateRoomId(){
       return connexionService.generateRoomId();
    }

}
