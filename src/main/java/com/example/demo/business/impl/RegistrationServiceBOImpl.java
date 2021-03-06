/**
 * @author - Chamath_Wijayarathna
 * Date :6/16/2022
 */

package com.example.demo.business.impl;

import com.example.demo.DTO.RegistrationRequestDTO;
import com.example.demo.business.RegistrationServiceBO;
import com.example.demo.entity.AppUser;
import com.example.demo.enumpackage.AppUserRoleEnum;
import com.example.demo.business.EmailSenderBO;
import com.example.demo.entity.ConfirmationToken;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;

import static com.example.demo.security.config.CommonConfig.URI;

@Service
@AllArgsConstructor
public class RegistrationServiceBOImpl implements RegistrationServiceBO {

    private final AppUserServiceBOImpl appUserServiceBOImpl;
    private final EmailValidatorBOImpl emailValidatorBOImpl;
    private final ConfirmationTokenServiceBOImpl confirmationTokenServiceBOImpl;
    private final EmailSenderBO emailSenderBO;


    @Autowired
    private RealEmailBOImpl realEmailBOImpl;

    @Override
    public String register(RegistrationRequestDTO request) { // Register User Json Object
        boolean isValidEmail = emailValidatorBOImpl.test(request.getEmail());

        if (!isValidEmail) {
            throw new IllegalStateException("email not valid");
        }

        String token = appUserServiceBOImpl.signUpUser(new AppUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(), AppUserRoleEnum.ROLE_USER

        ));

        String link = URI+"/api/v1/registration/confirm?token=" + token + "&name=" + request.getFirstName() + "&email=" + request.getEmail();
        emailSenderBO.send(request.getEmail(), buildEmail(request.getFirstName(), link));

        return token;
    }

    @Transactional
    @Override
    //  Confirmation email link will take token, name and email
    public String confirmToken(String token, String name, String email) {
        ConfirmationToken confirmationToken = confirmationTokenServiceBOImpl.getToken(token).orElseThrow(() -> new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenServiceBOImpl.setConfirmedAt(token);
        appUserServiceBOImpl.enableAppUser(confirmationToken.getAppUser().getEmail());
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                realEmailBOImpl.sendMailToNewAppUser(name, email);
            }
        });

        return "\n" + "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <title>Email Verification with Registration</title>\n" + "    <style>\n" + "        html,\n" + "        body {\n" + "            font-family: sans-serif;\n" + "            height: 100%;\n" + "            width: 100%;\n" + "            background: rgba(200,200,200,0.2);\n" + "        }\n" + "        .container-720 {\n" + "            max-width: 720px;\n" + "            margin: auto;\n" + "            background: #fff;\n" + "        }\n" + "\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "\n" + "<div class=\"container-720\">\n" + "    <div class=\"heading\" style=\"background: #5cb85c;display:flex; align-items: center;justify-content: space-between;margin-left:-20px;margin-right: -20px;box-shadow: 0 5px 10px -5px green;padding-left: 30px;padding-right:30px;\">\n" + "        <h1 style=\"line-height: 50px;padding-left: 10px;color:white\"> " + name + " Registration Successfully</h1>\n" + "        <img src=\"\" alt=\"\" style=\"height: 50px;\">\n" + "    </div>\n" + "    <main style=\"padding: 10px;\">\n" + "        <p>Hello New User,</p>\n" + "        <p>\n" + "            You have successfully registered for the System\n" + "        </p>\n" + "\n" + "        <p><i>\n" + "                    </i></p>\n" + "        <p>\n" + "\n" + "        </p>\n" + "        <p>\n" + "            <a\n" + "            </a>\n" + "        </p>\n" + "        <p>.</p>\n" + "    </main>\n" + "    <footer style=\"padding:20px 10px 20px 10px;font-size: 0.9em;border-top:2px double #1f3f1f;\">\n" + "\n" + "        <p>\n" + "Lorem   \n" + "        </p>\n" + "    </footer>\n" + "</div>\n" + "\n" + "</body>\n" + "</html>";
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" + "\n" + "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" + "\n" + "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" + "    <tbody><tr>\n" + "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" + "        \n" + "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" + "          <tbody><tr>\n" + "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" + "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" + "                  <tbody><tr>\n" + "                    <td style=\"padding-left:10px\">\n" + "                  \n" + "                    </td>\n" + "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" + "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" + "                    </td>\n" + "                  </tr>\n" + "                </tbody></table>\n" + "              </a>\n" + "            </td>\n" + "          </tr>\n" + "        </tbody></table>\n" + "        \n" + "      </td>\n" + "    </tr>\n" + "  </tbody></table>\n" + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" + "    <tbody><tr>\n" + "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" + "      <td>\n" + "        \n" + "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" + "                  <tbody><tr>\n" + "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" + "                  </tr>\n" + "                </tbody></table>\n" + "        \n" + "      </td>\n" + "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" + "    </tr>\n" + "  </tbody></table>\n" + "\n" + "\n" + "\n" + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" + "    <tbody><tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n" + "    <tr>\n" + "      <td width=\"10\" valign=\"middle\"><br></td>\n" + "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" + "        \n" + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 15 minutes. <p>See you soon</p>" + "        \n" + "      </td>\n" + "      <td width=\"10\" valign=\"middle\"><br></td>\n" + "    </tr>\n" + "    <tr>\n" + "      <td height=\"30\"><br></td>\n" + "    </tr>\n" + "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" + "\n" + "</div></div>";
    }
}
