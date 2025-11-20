package org.creditto.core_banking.domain.recipient.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipientFactory {

    private final RecipientRepository recipientRepository;

    // 수취인 정보를 받아, 기존에 동일한 정보의 수취인이 있으면 조회하고 없으면 새로 생성하여 반환
    @Transactional
    public Recipient findOrCreate(RecipientCreateDto dto) {
        return recipientRepository.findByBankCodeAndAccountNoAndName(
                dto.bankCode(),
                dto.accountNumber(),
                dto.name()
        ).orElseGet(() -> {
            Recipient newRecipient = Recipient.of(dto);
            return recipientRepository.save(newRecipient);
        });
    }
}
