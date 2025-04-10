package org.example.video_hosting.service;

import lombok.RequiredArgsConstructor;
import org.example.video_hosting.entity.Complaint;
import org.example.video_hosting.payload.ApiResponse;
import org.example.video_hosting.payload.constants.ResponseError;
import org.example.video_hosting.payload.constants.ResponseSuccess;
import org.example.video_hosting.payload.response.ResponsePageable;
import org.example.video_hosting.repository.ComplaintRepository;
import org.jvnet.hk2.annotations.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
x
    public ApiResponse<?> getComplaints(boolean cancelled, LocalDate startDate,int page,int size){
        Page<Complaint> complaintPage = complaintRepository.findByCancelledAndDateAfter(cancelled,startDate, PageRequest.of(page,size));
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

        complaintRepository.delete(complaint.get());
        return ApiResponse.ok(ResponseSuccess.deleted("Shikoyat"));
    }
}
