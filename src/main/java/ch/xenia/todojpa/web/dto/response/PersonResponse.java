package ch.xenia.todojpa.web.dto.response;

import ch.xenia.todojpa.domain.Person;
import ch.xenia.todojpa.domain.PersonRoleEnum;

import java.util.List;

public record PersonResponse(
        String email,
        String firstName,
        String lastName,
        String address,
        PersonRoleEnum role,
        List<AccountResponse> accounts
) {
    public static PersonResponse from(Person person) {
        List<AccountResponse> accountsDto = person.getAccounts() == null
                ? List.of()
                : person.getAccounts().stream().map(AccountResponse::from).toList();
        return new PersonResponse(
                person.getEmail(),
                person.getFirstName(),
                person.getLastName(),
                person.getAddress(),
                person.getRole(),
                accountsDto
        );
    }
}
