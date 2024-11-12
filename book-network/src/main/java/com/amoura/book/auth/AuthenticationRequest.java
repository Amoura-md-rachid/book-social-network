package com.amoura.book.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {

    @Email(message = "Email is not formatted !")
    @NotEmpty(message = "Email si mandatory")
    @NotBlank(message = "Email si mandatory")
    private String email;
    @Size(message = "Password shoud be 8 characters long minimum")
    @NotEmpty(message = "Password si mandatory")
    @NotBlank(message = "Password si mandatory")
    private String password;

    }
