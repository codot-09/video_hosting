package org.example.video_hosting.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.Complaint;
import org.example.video_hosting.entity.Content;
import org.example.video_hosting.entity.User;
import org.example.video_hosting.entity.enums.ComplaintType;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.request.ReqComplaint;
import org.example.video_hosting.payload.response.ResponsePageable;
import org.example.video_hosting.repository.ComplaintRepository;
import org.example.video_hosting.repository.ContentRepository;
import org.example.video_hosting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    public ApiResponse<?> getComplaints(ComplaintType type,boolean cancelled, LocalDate startDate,int page,int size){
        Page<Complaint> complaintPage = complaintRepository.findByCancelledAndDateAfterAndType(cancelled,startDate, PageRequest.of(page,size),type);
        if (complaintPage.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("Shikoyatlar"));
        }

        ResponsePageable<Object> responsePageable = ResponsePageable.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(complaintPage.getTotalPages())
                .totalElements(complaintPage.getTotalElements())
                .content(Collections.singletonList(complaintPage.getContent()))
                .build();

        return ApiResponse.ok(ResponseSuccess.fetched("Shikoyatlar"),responsePageable);
    }

    public ApiResponse<?> cancelComplaint(Long complaintId){
        Optional<Complaint> complaint = complaintRepository.findById(complaintId);
        if (complaint.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("Shikoyat"));
        }

        complaint.get().setCancelled(true);
        complaintRepository.save(complaint.get());
        return ApiResponse.ok(ResponseSuccess.deleted("Shikoyat"));
    }

    @Transactional
    public ApiResponse<?> banUser(ReqComplaint reqComplaint, ComplaintType type){
        User user = userRepository.findById(reqComplaint.userId()).orElse(null);
        Optional<Content> content = contentRepository.findById(reqComplaint.contentId());
        if (user == null || !user.isActive()){
            return ApiResponse.error(ResponseError.notFound("Foydalanuvchi"));
        } else if (user.getComplaintsCount() >= 10) {
            LocalDate endDate = LocalDate.now().plusDays(7);
            user.setBanned(true);
            user.setBanEndDate(endDate);
            userRepository.save(user);
        } else if (content.isEmpty()) {
            return ApiResponse.error(ResponseError.notFound("Content"));
        } else if (type.equals(ComplaintType.SPAM)) {
            return ApiResponse.error(ResponseError.defaultError("Spam shikoyati qabul qilinmadi"));
        }

        Complaint newComplaint = Complaint.builder()
                .type(type)
                .user(user)
                .description(!reqComplaint.description().isBlank() ? reqComplaint.description() : null)
                .content(content.orElse(null))
                .build();

        complaintRepository.save(newComplaint);

        user.setComplaintsCount(user.getComplaintsCount() + 1);
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.operationDone("Shikoyat qabul qilindi"));
    }

    public ApiResponse<?> unBanUser(Long userId){
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isActive()){
            return ApiResponse.error(ResponseError.notFound("Foydalanuvchi"));
        }

        user.setBanned(false);
        user.setBanEndDate(null);
        user.setComplaintsCount(0);
        userRepository.save(user);

        return ApiResponse.ok(ResponseSuccess.operationDone("Shikoyat qabul qilindi"));
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void autoUnBan(){
        List<User> bannedUsers = userRepository.findAllByBanEndDate(LocalDate.now());

        if (!bannedUsers.isEmpty()) {
            for (User user : bannedUsers) {
                user.setBanned(false);
                user.setBanEndDate(null);
                userRepository.save(user);

                System.out.println("Banned users unbanned");
            }
        }
        System.out.println("No banned users");
    }

    public ApiResponse<?> showBannedUsers(LocalDate startDate,LocalDate endDate){
        List<User> bannedUsers = userRepository.findAllByBannedTrueAndBanEndDateBetween(startDate,endDate).stream().toList();
        if (bannedUsers.isEmpty()){
            return ApiResponse.error(ResponseError.notFound("Bloklangan foydalanuvchilar"));
        }

        return ApiResponse.ok(ResponseSuccess.fetched("Bloklangan foydalanuvchilar"),bannedUsers);
    }
}
