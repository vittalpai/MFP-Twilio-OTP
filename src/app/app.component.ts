import { Component, Renderer } from '@angular/core';
import { Platform, App, AlertController } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';

import { HomePage } from '../pages/home/home';
@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  rootPage:any = HomePage;
  private TwilioOTPChallengeHandler: any

  constructor(platform: Platform, statusBar: StatusBar, splashScreen: SplashScreen, renderer: Renderer, public appCtrl: App, public alertCtrl: AlertController) {
    platform.ready().then(() => {
      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      statusBar.styleDefault();
      splashScreen.hide();

      // register mfp init function after plugin loaded
    renderer.listenGlobal('document', 'mfpjsloaded', () => {
      console.log('--> MobileFirst API plugin init complete');
      this.MFPInitComplete();
    });
    });
  }

   // MFP Init complete function
   MFPInitComplete() {
    console.log('--> MFPInitComplete function called');
    this.registerChallengeHandler();  // register a ChallengeHandler callback for TwilioOTP security check
  }

  registerChallengeHandler() {
    this.TwilioOTPChallengeHandler = WL.Client.createSecurityCheckChallengeHandler("TwilioOTP");
    this.TwilioOTPChallengeHandler.handleChallenge = ((challenge: any) => {
      console.log('--> TwilioOTPChallengeHandler.handleChallenge called');
      this.displayLoginChallenge(challenge);
    });
  }

  displayLoginChallenge(response) {
    if (response.errorMsg) {
      var msg = response.errorMsg;
      console.log('--> displayLoginChallenge ERROR: ' + msg);
    }
    let prompt = this.alertCtrl.create({
      title: msg,
      message: '',
      inputs: [
        {
          name: 'OTP',
          placeholder: 'enter the verification code'
        },
      ],
      buttons: [
        {
          text: 'Verify',
          handler: data => {
            data.phoneNumber = response.phoneNumber;
            data.countryCode = response.countryCode;
            this.TwilioOTPChallengeHandler.submitChallengeAnswer(data);
          }
        }, 
        {
          text: 'Cancel',
          handler: data => {
          this.TwilioOTPChallengeHandler.Cancel();
          }
        }
      ]
    });
    prompt.present();
  }
}

