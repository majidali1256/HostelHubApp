package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import com.hostelhub.utils.DateUtils
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AgreementRepository"

@Singleton
class AgreementRepository @Inject constructor(
    private val agreementApi: AgreementApi
) {
    private val demoAgreements = mutableListOf<Agreement>()
    private val demoTemplates = listOf(
        AgreementTemplate(
            id = "template_1",
            name = "Standard Rental Agreement",
            type = "rental",
            content = """
                HOSTEL RENTAL AGREEMENT
                
                This Rental Agreement is entered into between the Landlord and Tenant on the terms set forth below.
                
                1. PREMISES: The Landlord agrees to rent to the Tenant the premises located at [HOSTEL_LOCATION].
                
                2. TERM: This agreement shall commence on [START_DATE] and continue until [END_DATE].
                
                3. RENT: The Tenant agrees to pay monthly rent of PKR [RENT_AMOUNT] on or before the 5th of each month.
                
                4. SECURITY DEPOSIT: A security deposit of PKR [DEPOSIT_AMOUNT] is required.
                
                5. UTILITIES: [UTILITIES_LIST]
                
                6. RULES AND REGULATIONS: The Tenant agrees to abide by all hostel rules.
            """.trimIndent(),
            terms = listOf(
                AgreementTerm("Payment Terms", "Rent must be paid by the 5th of each month", true),
                AgreementTerm("Notice Period", "One month notice required before vacating", true),
                AgreementTerm("Maintenance", "Tenant responsible for minor maintenance", false)
            )
        )
    )

    private fun seedDemoAgreementsIfNeeded() {
        if (demoAgreements.isNotEmpty()) return
        demoAgreements.addAll(
            listOf(
                Agreement(
                    id = "agr_1",
                    bookingId = "booking_101",
                    hostelId = "hostel_1",
                    landlordId = "owner_1",
                    tenantId = "student_1",
                    hostel = AgreementHostelInfo(id = "hostel_1", name = "Sunrise Luxury Boys Hostel", location = "Sector H-12, Islamabad"),
                    landlord = AgreementUserInfo(id = "owner_1", firstName = "Chaudhry", lastName = "Aslam", email = "aslam@sunrise.com", contactNumber = "+92 300 1234567"),
                    tenant = AgreementUserInfo(id = "student_1", firstName = "Ali", lastName = "Raza", email = "ali.raza@student.nust.edu.pk", contactNumber = "+92 333 9876543"),
                    type = AgreementType.RENTAL,
                    title = "Tenancy Agreement - Executive Single Room #204",
                    content = """
                        HOSTEL TENANCY AGREEMENT
                        
                        This Tenancy Agreement ("Agreement") is entered into on January 1, 2024, by and between:
                        Landlord: Chaudhry Aslam (Owner of Sunrise Luxury Boys Hostel, Sector H-12, Islamabad)
                        Tenant: Ali Raza (Student ID / CNIC: 37405-1234567-9)
                        
                        1. PREMISES & ROOM ALLOCATION
                        The Landlord hereby leases to the Tenant Executive Single Room #204 located at Sunrise Luxury Boys Hostel, Sector H-12, Islamabad, along with shared access to common areas, kitchen, laundry, and study lounges.
                        
                        2. TERM OF TENANCY
                        The tenancy shall commence on January 1, 2024 ("Check-In Date") and shall continue until December 31, 2024 ("Check-Out Date"), unless terminated earlier in accordance with the terms herein.
                        
                        3. RENT & PAYMENT SCHEDULE
                        The Tenant agrees to pay a fixed monthly rent of PKR 35,000. Rent is due strictly on or before the 5th day of each calendar month. Payments made after the 5th will incur a late surcharge of PKR 500 per day.
                        
                        4. SECURITY DEPOSIT
                        Upon signing this Agreement, the Tenant shall deposit PKR 70,000 (equivalent to two months' rent) as a refundable security deposit. This deposit will be refunded within 14 days of checkout, after deducting any charges for property damage or unpaid utility dues.
                        
                        5. INCLUDED UTILITIES & AMENITIES
                        The monthly rent includes high-speed fiber WiFi, backup UPS/generator electricity, geyser hot water, filtered drinking water, and daily room cleaning services. Dedicated air conditioner (AC) electricity units will be billed separately via sub-meter at the prevailing commercial unit tariff.
                        
                        6. HOSTEL HOUSE RULES & CONDUCT
                        - Smoking, vaping, illegal substances, and alcohol are strictly prohibited anywhere on the property.
                        - Main hostel gates lock daily at 11:30 PM. Overnight late entry requires prior permission from the hostel warden.
                        - Visitors and guests are allowed only in the ground floor reception lounge between 10:00 AM and 8:00 PM. No guests are permitted in residential rooms.
                        - Quiet hours must be observed between 10:00 PM and 7:00 AM to ensure an optimal study and rest environment.
                        
                        7. TERMINATION & VACATING
                        Either party may terminate this Agreement by providing a minimum of 30 days' advance written notice. If the Tenant vacates without 30 days' notice, one month's security deposit shall be forfeited.
                    """.trimIndent(),
                    terms = listOf(
                        AgreementTerm("Monthly Rent Payment", "PKR 35,000 due by the 5th of each calendar month.", true),
                        AgreementTerm("Security Deposit Refund", "PKR 70,000 deposit refundable upon clean room handover.", true),
                        AgreementTerm("Gate & Quiet Hours Policy", "Gate closes at 11:30 PM; quiet hours enforced after 10:00 PM.", true),
                        AgreementTerm("Sub-meter AC Billing", "Air conditioner electrical units billed separately each month.", true)
                    ),
                    duration = AgreementDuration(startDate = "2024-01-01", endDate = "2024-12-31"),
                    rentAmount = 35000.0,
                    deposit = 70000.0,
                    status = AgreementStatus.ACTIVE,
                    signatures = listOf(
                        SignatureRecord(userId = "owner_1", role = "landlord", signatureId = "sig_owner_101", signatureUrl = "https://demo.hostelhub.com/sigs/owner1.png", signedAt = "2024-01-01T10:00:00Z"),
                        SignatureRecord(userId = "student_1", role = "tenant", signatureId = "sig_student_101", signatureUrl = "https://demo.hostelhub.com/sigs/student1.png", signedAt = "2024-01-01T14:30:00Z")
                    ),
                    metadata = AgreementMetadata(
                        utilities = listOf("High-Speed WiFi", "UPS Backup", "Geyser Hot Water", "Daily Cleaning"),
                        rules = listOf("No Smoking/Vaping", "Gate closed at 11:30 PM", "No room guests"),
                        paymentMethod = "Bank Transfer / EasyPaisa",
                        lateFeePolicy = "PKR 500/day after 5th of each month"
                    ),
                    createdAt = "2024-01-01T09:00:00Z",
                    signedAt = "2024-01-01T14:30:00Z"
                ),
                Agreement(
                    id = "agr_2",
                    bookingId = "booking_102",
                    hostelId = "hostel_2",
                    landlordId = "owner_2",
                    tenantId = "student_1",
                    hostel = AgreementHostelInfo(id = "hostel_2", name = "Green Valley Girls & Executive Residency", location = "Johar Town, Lahore"),
                    landlord = AgreementUserInfo(id = "owner_2", firstName = "Malik", lastName = "Tariq", email = "tariq@greenvalley.pk", contactNumber = "+92 321 8889900"),
                    tenant = AgreementUserInfo(id = "student_1", firstName = "Ali", lastName = "Raza", email = "ali.raza@student.nust.edu.pk", contactNumber = "+92 333 9876543"),
                    type = AgreementType.RENTAL,
                    title = "Tenancy Agreement - Double Shared Room #105",
                    content = """
                        HOSTEL TENANCY AGREEMENT
                        
                        This Tenancy Agreement ("Agreement") is entered into on February 1, 2024, by and between:
                        Landlord: Malik Tariq (Green Valley Girls & Executive Residency, Johar Town, Lahore)
                        Tenant: Ali Raza
                        
                        1. PREMISES
                        The Landlord leases to the Tenant Double Shared Room #105 located at Green Valley Residency, Johar Town, Lahore.
                        
                        2. TERM
                        Tenancy duration is from February 1, 2024 to January 31, 2025.
                        
                        3. RENT & SECURITY DEPOSIT
                        Monthly Rent: PKR 24,000 (due by the 5th of every month).
                        Security Deposit: PKR 48,000 (refundable upon completion of lease term).
                        
                        4. SIGNATURE REQUIREMENT
                        This contract has been generated and approved by the Landlord. Please review the terms carefully and use the digital signature canvas below to sign and finalize your tenancy.
                    """.trimIndent(),
                    terms = listOf(
                        AgreementTerm("Monthly Rent", "PKR 24,000 payable by the 5th of each month.", true),
                        AgreementTerm("Security Deposit", "PKR 48,000 refundable security deposit.", true),
                        AgreementTerm("Digital Signature Binding", "Your e-signature constitutes legally binding acceptance of all hostel rules.", true)
                    ),
                    duration = AgreementDuration(startDate = "2024-02-01", endDate = "2025-01-31"),
                    rentAmount = 24000.0,
                    deposit = 48000.0,
                    status = AgreementStatus.PENDING,
                    signatures = listOf(
                        SignatureRecord(userId = "owner_2", role = "landlord", signatureId = "sig_owner_102", signatureUrl = "https://demo.hostelhub.com/sigs/owner2.png", signedAt = "2024-02-01T11:15:00Z")
                    ),
                    metadata = AgreementMetadata(
                        utilities = listOf("WiFi", "Generator", "Attached Bath"),
                        rules = listOf("Quiet hours after 10 PM", "No unauthorized visitors"),
                        paymentMethod = "JazzCash / Bank Transfer",
                        lateFeePolicy = "PKR 300/day after due date"
                    ),
                    createdAt = "2024-02-01T11:15:00Z",
                    signedAt = null
                )
            )
        )
    }

    suspend fun getAgreements(): Result<List<Agreement>> {
        seedDemoAgreementsIfNeeded()
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoAgreements)
        }

        return try {
            val response = agreementApi.getAgreements()
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.isNotEmpty()) {
                    demoAgreements.clear()
                    demoAgreements.addAll(response.body()!!)
                }
                Result.success(demoAgreements.ifEmpty { response.body()!! })
            } else {
                Result.success(demoAgreements)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching agreements", e)
            Result.success(demoAgreements)
        }
    }

    suspend fun getMyAgreements(): Result<List<Agreement>> {
        seedDemoAgreementsIfNeeded()
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            return Result.success(demoAgreements)
        }

        return try {
            val response = agreementApi.getMyAgreements()
            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.isNotEmpty()) {
                    demoAgreements.clear()
                    demoAgreements.addAll(response.body()!!)
                }
                Result.success(demoAgreements.ifEmpty { response.body()!! })
            } else {
                Result.success(demoAgreements)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching my agreements, falling back to demo cache", e)
            Result.success(demoAgreements)
        }
    }

    suspend fun generateAgreement(
        bookingId: String,
        hostelId: String,
        studentId: String,
        ownerId: String,
        termsAndConditions: String,
        monthlyRent: Double
    ): Result<Agreement> {
        seedDemoAgreementsIfNeeded()
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            val newAgreement = Agreement(
                id = "demo_gen_${System.currentTimeMillis()}",
                bookingId = bookingId,
                hostelId = hostelId,
                landlordId = ownerId,
                tenantId = studentId,
                hostel = AgreementHostelInfo(id = hostelId, name = "Hostel Hub Verified Property", location = "City Center"),
                landlord = AgreementUserInfo(id = ownerId, firstName = "Property", lastName = "Owner"),
                tenant = AgreementUserInfo(id = studentId, firstName = "Student", lastName = "Resident"),
                type = AgreementType.RENTAL,
                title = "Standard Digital Tenancy Contract",
                content = termsAndConditions.ifBlank {
                    """
                    HOSTEL DIGITAL TENANCY CONTRACT
                    
                    Parties Involved:
                    Landlord ID: $ownerId
                    Tenant ID: $studentId
                    
                    Terms & Agreement Clauses:
                    1. The Tenant agrees to pay PKR $monthlyRent on or before the 5th of each calendar month.
                    2. Security deposit equal to two months' rent is payable prior to check-in.
                    3. Both parties agree to abide by official Hostel Hub community rules, quiet hours, and visitor protocols.
                    """.trimIndent()
                },
                terms = listOf(
                    AgreementTerm("Monthly Rent", "PKR $monthlyRent due on 5th of every month.", true),
                    AgreementTerm("Digital Signature", "Legally binding electronic signature.", true)
                ),
                duration = AgreementDuration(startDate = DateUtils.getCurrentTimestamp().take(10), endDate = "2025-12-31"),
                rentAmount = monthlyRent,
                deposit = monthlyRent * 2,
                status = AgreementStatus.PENDING,
                signatures = listOf(
                    SignatureRecord(userId = ownerId, role = "landlord", signatureId = "sig_owner_auto", signatureUrl = null, signedAt = DateUtils.getCurrentTimestamp())
                ),
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoAgreements.add(0, newAgreement)
            return Result.success(newAgreement)
        }

        return try {
            val request = GenerateAgreementRequest(bookingId, hostelId, studentId, ownerId, termsAndConditions, monthlyRent)
            val response = agreementApi.generateAgreement(request)
            if (response.isSuccessful && response.body() != null) {
                demoAgreements.add(0, response.body()!!)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to generate agreement from server"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating agreement", e)
            Result.failure(Exception("Failed to generate agreement: ${e.message}"))
        }
    }

    suspend fun getAgreement(id: String): Result<Agreement> {
        seedDemoAgreementsIfNeeded()
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val agreement = demoAgreements.find { it.id == id }
            return if (agreement != null) {
                Result.success(agreement)
            } else {
                Result.failure(Exception("Agreement not found"))
            }
        }

        return try {
            val response = agreementApi.getAgreement(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Agreement not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAgreement(
        bookingId: String,
        hostelId: String,
        tenantId: String,
        title: String,
        content: String,
        terms: List<AgreementTerm>,
        duration: AgreementDuration,
        rentAmount: Double,
        deposit: Double,
        metadata: AgreementMetadata? = null
    ): Result<Agreement> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            val newAgreement = Agreement(
                id = "demo_agreement_${System.currentTimeMillis()}",
                bookingId = bookingId,
                hostelId = hostelId,
                landlordId = "demo_owner",
                tenantId = tenantId,
                hostel = AgreementHostelInfo(id = hostelId, name = "Demo Hostel", location = "Lahore"),
                landlord = AgreementUserInfo(id = "demo_owner", firstName = "Hostel", lastName = "Owner"),
                tenant = AgreementUserInfo(id = tenantId, firstName = "Demo", lastName = "Tenant"),
                type = AgreementType.RENTAL,
                title = title,
                content = content,
                terms = terms,
                duration = duration,
                rentAmount = rentAmount,
                deposit = deposit,
                status = AgreementStatus.PENDING,
                signatures = emptyList(),
                metadata = metadata,
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoAgreements.add(newAgreement)
            return Result.success(newAgreement)
        }

        return try {
            val request = CreateAgreementRequest(
                bookingId, hostelId, tenantId, "rental", title, content, terms, duration, rentAmount, deposit, metadata
            )
            val response = agreementApi.createAgreement(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create agreement"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating agreement", e)
            Result.failure(Exception("Failed to create agreement: ${e.message}"))
        }
    }

    suspend fun signAgreement(id: String, signatureData: String): Result<Agreement> {
        seedDemoAgreementsIfNeeded()
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            val index = demoAgreements.indexOfFirst { it.id == id }
            if (index >= 0) {
                val current = demoAgreements[index]
                val newSignature = SignatureRecord(
                    userId = "demo_user",
                    role = "tenant",
                    signatureId = "sig_${System.currentTimeMillis()}",
                    signatureUrl = "demo_signature.png",
                    signedAt = DateUtils.getCurrentTimestamp()
                )
                val updated = current.copy(
                    signatures = current.signatures + newSignature,
                    status = if (current.signatures.size >= 1) AgreementStatus.SIGNED else AgreementStatus.PENDING,
                    signedAt = if (current.signatures.size >= 1) DateUtils.getCurrentTimestamp() else null
                )
                demoAgreements[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Agreement not found"))
        }

        return try {
            val response = agreementApi.signAgreement(id, SignAgreementRequest(signatureData))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to sign agreement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAgreementTemplates(): Result<List<AgreementTemplate>> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(demoTemplates)
        }

        return try {
            val response = agreementApi.getAgreementTemplates()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch templates"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDirectAgreement(
        tenantId: String,
        hostelId: String? = null,
        title: String,
        content: String,
        terms: List<AgreementTerm>,
        duration: AgreementDuration,
        rentAmount: Double,
        deposit: Double
    ): Result<Agreement> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            val newAgreement = Agreement(
                id = "demo_direct_agreement_${System.currentTimeMillis()}",
                hostelId = hostelId ?: "",
                landlordId = "demo_owner",
                tenantId = tenantId,
                title = title,
                content = content,
                terms = terms,
                duration = duration,
                rentAmount = rentAmount,
                deposit = deposit,
                status = AgreementStatus.PENDING,
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoAgreements.add(newAgreement)
            return Result.success(newAgreement)
        }

        return try {
            val request = CreateDirectAgreementRequest(tenantId, hostelId, title, content, terms, duration, rentAmount, deposit)
            val response = agreementApi.createDirectAgreement(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create direct agreement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun terminateAgreement(id: String, reason: String): Result<Agreement> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoAgreements.indexOfFirst { it.id == id }
            if (index >= 0) {
                val updated = demoAgreements[index].copy(status = AgreementStatus.TERMINATED)
                demoAgreements[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Agreement not found"))
        }

        return try {
            val response = agreementApi.terminateAgreement(id, TerminateAgreementRequest(reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to terminate agreement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

