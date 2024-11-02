package com.encurtador_url.controller;

import com.encurtador_url.dtos.EncurtadorUrlRequest;
import com.encurtador_url.dtos.EncurtadorUrlResponse;
import com.encurtador_url.entities.UrlEntity;
import com.encurtador_url.repository.UrlRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
public class UrlController {
    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @PostMapping("/shorten-url")
    public ResponseEntity<EncurtadorUrlResponse> encurtadorUrl(@RequestBody EncurtadorUrlRequest request,
                                              HttpServletRequest servletRequest) {
        String id;
        do {
            id = RandomStringUtils.randomAlphanumeric(5, 10);
        } while (urlRepository.existsById(id));

        urlRepository.save(new UrlEntity(id, request.url(), LocalDateTime.now().plusMinutes(1)));

        var redirectUrl = servletRequest.getRequestURL().toString().replace("shorten-url", id);

        return ResponseEntity.ok(new EncurtadorUrlResponse(redirectUrl));
    }

    @GetMapping("{id}")
    public ResponseEntity<Void> redirecionar(@PathVariable("id") String request) {
        var id = urlRepository.findById(request).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(id.getFullUrl()));
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

}
