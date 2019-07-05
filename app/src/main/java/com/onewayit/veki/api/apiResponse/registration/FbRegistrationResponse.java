package com.onewayit.veki.api.apiResponse.registration;
import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FbRegistrationResponse implements Serializable
{
    @SerializedName("register")
    @Expose
    private Register register;
            @SerializedName("status_code")
    @Expose
    private String statusCode;
            @SerializedName("status")
    @Expose
    private String status;
            @SerializedName("message")
    @Expose

            private String message;
            @SerializedName("data")
   @Expose
    private Object data;
           private final static long serialVersionUID = 4491963882504513622L;

            public Register getRegister() {
                return register;
            }

            public void setRegister(Register register) {
               this.register = register;
            }

            public String getStatusCode() {
                return statusCode;
            }

            public void setStatusCode(String statusCode) {
                this.statusCode = statusCode;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

           public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
            public Object getdata() {
                return data ;
            }

            public void setdata(Object data) {
                this.data = data;
            }

        }