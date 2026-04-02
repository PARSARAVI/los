package com.narvee.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping("/serviceDown")
	public ResponseEntity<String> ServerDown(Exception e) {
		return new ResponseEntity<String>(
				"Service not Available. Please try again later or contact our support team.",
				HttpStatus.SERVICE_UNAVAILABLE);
	}

	@PostMapping("/serviceDown")
	public ResponseEntity<String> ServerDownPost() {
		return new ResponseEntity<String>(
				"Service not Available. Please try again later or contact our support team.",
				HttpStatus.SERVICE_UNAVAILABLE);
	}

}