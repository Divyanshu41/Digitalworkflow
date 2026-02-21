package com.example.digitalapproval.dto;

import java.time.LocalDateTime;

public class RequestResponseDto {

        private Long id;
        private String title;
        private String description;
        private String status;
        private String remarks;
        private LocalDateTime submittedAt;
        private Long userId;

        public RequestResponseDto() {
        }

        public RequestResponseDto(Long id, String title, String description, String status, String remarks,
                                                          LocalDateTime submittedAt, Long userId) {
                this.id = id;
                this.title = title;
                this.description = description;
                this.status = status;
                this.remarks = remarks;
                this.submittedAt = submittedAt;
                this.userId = userId;
        }

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getTitle() {
                return title;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public String getDescription() {
                return description;
        }

        public void setDescription(String description) {
                this.description = description;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public String getRemarks() {
                return remarks;
        }

        public void setRemarks(String remarks) {
                this.remarks = remarks;
        }

        public LocalDateTime getSubmittedAt() {
                return submittedAt;
        }

        public void setSubmittedAt(LocalDateTime submittedAt) {
                this.submittedAt = submittedAt;
        }

        public Long getUserId() {
                return userId;
        }

        public void setUserId(Long userId) {
                this.userId = userId;
        }
}
