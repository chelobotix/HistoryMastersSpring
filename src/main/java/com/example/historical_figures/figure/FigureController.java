package com.example.historical_figures.figure;

import com.example.historical_figures.user.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/figures")
public class FigureController {

    @GetMapping()
    public ResponseEntity<?> list() {
        return ResponseEntity.ok().body("ok");
    }
}
