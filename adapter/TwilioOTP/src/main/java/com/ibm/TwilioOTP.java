/**
* Copyright 2018 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.ibm;

import com.ibm.mfp.security.checks.base.UserAuthenticationSecurityCheck;
import com.ibm.mfp.server.registration.external.model.AuthenticatedUser;
import com.ibm.mfp.server.security.external.checks.SecurityCheckConfiguration;
import com.authy.*;
import com.authy.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TwilioOTP extends UserAuthenticationSecurityCheck {
    private String userId, displayName;
    private String errorMsg;
    private String number;
    private String numberCountryCode;
    private boolean rememberMe = false;

    @Override
    public SecurityCheckConfiguration createConfiguration(Properties properties) {
        return new TwilioConfig(properties);
    }
    
    @Override
    protected TwilioConfig getConfiguration() {
        return (TwilioConfig) super.getConfiguration();
    }

    @Override
    protected AuthenticatedUser createUser() {
        return new AuthenticatedUser(userId, displayName, this.getName());
    }

    @Override
    protected boolean validateCredentials(Map<String, Object> credentials) {
        if(credentials!=null && credentials.containsKey("OTP") && credentials.containsKey("phoneNumber")){
            String OTP = credentials.get("OTP").toString();
            String phonenumber = credentials.get("phoneNumber").toString();
            String countryCode = credentials.get("countryCode").toString();
            AuthyApiClient client = new AuthyApiClient(getConfiguration().twilioAPIKey);
            PhoneVerification phoneVerification = client.getPhoneVerification();
            if (OTP!=null && phonenumber!=null && OTP!="" && phonenumber!="" && countryCode!=null && countryCode!="") {
                Verification verification;
                try {
                verification = phoneVerification.check(phonenumber, countryCode, OTP);
                if (verification.isOk()) {
                        return true;
                    } else {
                        errorMsg = "Incorrect OTP, Try again";
                        return false;
                    }
            } catch (AuthyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } }
          }
          else if(credentials!=null && credentials.containsKey("phoneNumber")){
              String phoneNumber = credentials.get("phoneNumber").toString();
              String countryCode = credentials.get("countryCode").toString();
              AuthyApiClient client = new AuthyApiClient(getConfiguration().twilioAPIKey);
              PhoneVerification phoneVerification  = client.getPhoneVerification();
              Verification verification;
              Params params = new Params();
              params.setAttribute("locale", "en");
              if (phoneNumber!=null && phoneNumber!="" && countryCode!=null && countryCode!="") {
                try {
                    verification = phoneVerification.start(phoneNumber, countryCode, "sms", params);
                     if (verification.isOk())  {
                         errorMsg = "Verfication code succesfully sent";
                         number = phoneNumber;
                         numberCountryCode = countryCode;
                         return false;
                     }
                } catch (AuthyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
              }
              
            } 
        else{
            errorMsg = "Invalid JSON, Send proper phone number";
        }
        errorMsg = "Invalid JSON, send proper phone number";
        return false;
    }

    @Override
    protected Map<String, Object> createChallenge() {
        Map challenge = new HashMap();
        challenge.put("errorMsg",errorMsg);
        challenge.put("phoneNumber",number);
        challenge.put("countryCode",numberCountryCode);
        challenge.put("remainingAttempts",getRemainingAttempts());
        return challenge;
    }

    @Override
    protected boolean rememberCreatedUser() {
        return rememberMe;
    }
}
