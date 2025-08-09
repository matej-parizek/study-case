package cz.matej.parizek.studyCase.eligibility

import cz.matej.parizek.eligibility.model.*
import net.datafaker.Faker
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object TestDataFactory {
    private val faker = Faker()

    fun uuid(): String = UUID.randomUUID().toString()

    fun birthDateAdult(): String =
        LocalDate.now().minusYears((18L..75L).random()).format(DateTimeFormatter.ISO_DATE)

    fun birthDateMinor(): String =
        LocalDate.now().minusYears((5L..17L).random()).format(DateTimeFormatter.ISO_DATE)

    fun clientDetail(
        clientId: String = uuid(),
        birthDateIso: String = birthDateAdult(),
        gender: GetClientDetailResponse.Gender =
            GetClientDetailResponse.Gender.entries.random()
    ): GetClientDetailResponse =
        GetClientDetailResponse(
            clientId = clientId,
            birthDate = birthDateIso,
            forename = faker.name().firstName(),
            surname = faker.name().lastName(),
            gender = gender,
            pep = false,
            clientVerificationLevel = 3,
            primaryEmail = faker.internet().emailAddress(),
            primaryPhone = faker.phoneNumber().cellPhone(),
            verifiedBy = faker.number().digits(8)
        )

    fun accountsResponse(
        clientId: String,
        accountsCount: Int = 1
    ): GetAccountsResponse {
        val client = Client(
            forename = faker.name().firstName(),
            surname = faker.name().lastName(),
            clientId = clientId
        )
        val accounts = (1..accountsCount).map {
            Account(
                productId = "SB0_${faker.number().digits(5)}",
                closingDate = null,
                prefix = faker.number().digits(4),
                number = faker.number().digits(6),
                bankCode = faker.number().digits(4),
                iban = "CZ${faker.number().digits(22)}",
                currency = Currency.CZK
            )
        }
        return GetAccountsResponse(client = client, accounts = accounts)
    }

    fun eligibilityResponse(
        eligible: Boolean = true,
        reasons: List<GetEligibilityResponse.Reasons>? = null
    ) = GetEligibilityResponse(eligible = eligible, reasons = reasons)
}
