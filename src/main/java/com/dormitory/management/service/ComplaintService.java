package com.dormitory.management.service;

import com.dormitory.management.dao.ApplicationDao;
import com.dormitory.management.dao.ComplaintDao;
import com.dormitory.management.model.AdminComplaintView;
import com.dormitory.management.model.Application;
import com.dormitory.management.model.ApplicationStatus;
import com.dormitory.management.util.OperationResult;

import java.util.List;
import java.util.Optional;

public class ComplaintService {
    private static final int MAX_COMPLAINT_LENGTH = 2000;

    private final ComplaintDao complaintDao = new ComplaintDao();
    private final ApplicationDao applicationDao = new ApplicationDao();

    public OperationResult submitComplaint(long userId, String message) {
        String trimmedMessage = message == null ? "" : message.trim();
        if (trimmedMessage.isEmpty()) {
            return OperationResult.failure("Complaint message is required.");
        }

        if (trimmedMessage.length() > MAX_COMPLAINT_LENGTH) {
            return OperationResult.failure("Complaint can have up to " + MAX_COMPLAINT_LENGTH + " characters.");
        }

        Optional<Application> applicationOptional = applicationDao.findByUserId(userId);
        if (applicationOptional.isEmpty()) {
            return OperationResult.failure("You need an approved dormitory assignment before sending complaints.");
        }

        Application application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACCEPTED || application.getDormitoryId() == null) {
            return OperationResult.failure("Complaints can be sent only after you are assigned to a dormitory.");
        }

        boolean created = complaintDao.createComplaint(userId, application.getDormitoryId(), trimmedMessage);
        return created
                ? OperationResult.success("Complaint submitted successfully.")
                : OperationResult.failure("Could not submit complaint right now.");
    }

    public List<AdminComplaintView> getComplaintsForAdmin() {
        return complaintDao.findAllForAdmin();
    }
}
