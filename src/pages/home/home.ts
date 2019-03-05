import { Component, NgZone } from '@angular/core';
import { NavController } from 'ionic-angular';

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  
  result: string = "";
  phoneNumber: string = "";
  securityCheck: string = "TwilioOTP";

  constructor(public navCtrl: NavController, private zone: NgZone) {
    console.log('--> HomePage constructor')
  }

  register() {
    var credentials = {
      phoneNumber : this.phoneNumber,
      countryCode : '1'
    };
    this.zone.run(() => {
        this.result = "";
    });
    WLAuthorizationManager.login(this.securityCheck, credentials).then(() => {
      console.log('-->  Phone Number Registration: Success ');
      this.zone.run(() => {
        this.result = "Phone Number Successfully verifed";
        this.phoneNumber = "";
      });
    },
    function(error){
      console.log('--> Phone Registration:  ERROR ', error.responseText);
      this.zone.run(() => {
        this.result = "Phone Number Verification Failed";
        this.phoneNumber = "";
      });
    });
  }

}
