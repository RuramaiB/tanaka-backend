package com.urban.settlement.auth;

import com.urban.settlement.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

  private String firstname;
  private String lastname;
  private String email;
  private String dateOfBirth;
  private String gender; // Changed to String to simplify if Gender enum is tricky, but kept compatible
  private String physicalAddress;
  private String password;
  private Role role;
  private boolean enabled;
}
