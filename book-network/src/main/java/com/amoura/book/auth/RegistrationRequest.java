package com.amoura.book.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class RegistrationRequest {

    @NotEmpty(message = "Firstname si mandatory")
    @NotBlank(message = "Firstname si mandatory")
    private String firstname;
    @NotEmpty(message = "Lastname si mandatory")
    @NotBlank(message = "Lastname si mandatory")
    private String lastname;
    @Email(message = "Email is not formatted !")
    @NotEmpty(message = "Email si mandatory")
    @NotBlank(message = "Email si mandatory")
    private String email;
    @Size(message = "Password shoud be 8 characters long minimum")
    @NotEmpty(message = "Password si mandatory")
    @NotBlank(message = "Password si mandatory")
    private String password;
}
