/**
 * @author - Chamath_Wijayarathna
 * Date :6/16/2022
 */

package com.example.demo.registration;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class EmailValidator implements Predicate<String> {
    @Override
    public boolean test(String s) {
//        TODO: Regex to validate email
        return true;
    }
}
