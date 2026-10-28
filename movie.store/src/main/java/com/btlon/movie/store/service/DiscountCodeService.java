package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.DiscountCodeRequest;
import com.btlon.movie.store.entity.DiscountCode;
import com.btlon.movie.store.repository.DiscountCodeRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class DiscountCodeService {
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Z0-9_-]{3,30}");
    private final DiscountCodeRepository codes;
    private final TicketRepository tickets;

    public DiscountCodeService(DiscountCodeRepository codes, TicketRepository tickets) {
        this.codes = codes;
        this.tickets = tickets;
    }

    public List<DiscountCode> list() {
        return codes.findAllByOrderByIdDesc();
    }

    /** Returns the usable code for today, or null when no code was entered. */
    public DiscountCode findUsable(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) return null;
        DiscountCode code = codes.findByCodeIgnoreCase(rawCode.trim())
                .orElseThrow(() -> new BadRequestException("Discount code does not exist"));
        if (!code.isUsableOn(LocalDate.now()))
            throw new BadRequestException("Discount code is inactive, expired or used up");
        return code;
    }

    /** Consumes one use inside the caller's booking transaction. */
    @Transactional
    public void consume(DiscountCode code) {
        if (codes.consumeOne(code.getId()) == 0)
            throw new ConflictException("Discount code is used up");
    }

    @Transactional
    public DiscountCode create(DiscountCodeRequest request) {
        String code = validate(request);
        if (codes.existsByCodeIgnoreCase(code)) throw new ConflictException("Discount code already exists");
        DiscountCode entity = new DiscountCode();
        apply(entity, request, code);
        return codes.save(entity);
    }

    @Transactional
    public Optional<DiscountCode> update(Long id, DiscountCodeRequest request) {
        String code = validate(request);
        return codes.findById(id).map(entity -> {
            codes.findByCodeIgnoreCase(code).filter(other -> !other.getId().equals(id)).ifPresent(other -> {
                throw new ConflictException("Discount code already exists");
            });
            if (request.getUsageLimit() != null && request.getUsageLimit() < entity.getUsedCount())
                throw new BadRequestException(
                        "usageLimit must not be lower than used count (" + entity.getUsedCount() + ")");
            apply(entity, request, code);
            return codes.save(entity);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!codes.existsById(id)) return false;
        if (tickets.existsByDiscountCode_Id(id))
            throw new ConflictException("Cannot delete a discount code used by tickets; deactivate it instead");
        codes.deleteById(id);
        return true;
    }

    private static void apply(DiscountCode entity, DiscountCodeRequest request, String code) {
        entity.setCode(code);
        entity.setDescription(request.getDescription() == null || request.getDescription().isBlank()
                ? null
                : request.getDescription().trim());
        entity.setDiscountPercent(request.getDiscountPercent());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setUsageLimit(request.getUsageLimit());
        entity.setActive(request.getActive() == null || request.getActive());
    }

    private static String validate(DiscountCodeRequest request) {
        if (request == null || request.getCode() == null)
            throw new BadRequestException("code is required");
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (!CODE_PATTERN.matcher(code).matches())
            throw new BadRequestException("code must be 3-30 characters: A-Z, 0-9, _ or -");
        if (request.getDiscountPercent() == null || request.getDiscountPercent() < 1
                || request.getDiscountPercent() > 100)
            throw new BadRequestException("discountPercent must be between 1 and 100");
        if (request.getDescription() != null && request.getDescription().trim().length() > 255)
            throw new BadRequestException("description must be at most 255 characters");
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate()))
            throw new BadRequestException("endDate must not be before startDate");
        if (request.getUsageLimit() != null && request.getUsageLimit() < 1)
            throw new BadRequestException("usageLimit must be greater than 0");
        return code;
    }
}
