package com.example.digitalapproval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequestCreateDto {

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        private String title;

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        private String description;

        @NotNull(message = "User id is required")
        private Long userId;

        public RequestCreateDto() {
        }

        public String getTitle() {
                return title;
        }

        public String title() {
                return title;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public String getDescription() {
                return description;
        }

        public String description() {
                return description;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public Long getUserId() {
                return userId;
        }

        public Long userId() {
                return userId;
        }

        public void setUserId(Long userId) {
                this.userId = userId;
        }
}
