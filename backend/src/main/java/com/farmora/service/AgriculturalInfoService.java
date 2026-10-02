package com.farmora.service;

import com.farmora.dto.agri.AgriculturalPostRequest;
import com.farmora.dto.agri.AgriculturalPostResponse;
import com.farmora.exception.AccessDeniedCustomException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.AgriculturalPostRepository;
import com.farmora.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgriculturalInfoService {

    private final AgriculturalPostRepository postRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    public List<AgriculturalPostResponse> getAllPosts(AgriCategory category, String province) {
        List<AgriculturalPost> posts;
        if (category != null && province != null && !province.isBlank()) {
            posts = postRepository.findByCategoryAndProvinceOrderByIsPinnedDescCreatedAtDesc(category, province);
        } else if (category != null) {
            posts = postRepository.findByCategoryOrderByIsPinnedDescCreatedAtDesc(category);
        } else if (province != null && !province.isBlank()) {
            posts = postRepository.findByProvinceOrderByIsPinnedDescCreatedAtDesc(province);
        } else {
            posts = postRepository.findAllByOrderByIsPinnedDescCreatedAtDesc();
        }

        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AgriculturalPostResponse getPostById(Long id) {
        AgriculturalPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agricultural post not found with id: " + id));
        return mapToResponse(post);
    }

    @Transactional
    public AgriculturalPostResponse createPost(AgriculturalPostRequest request, String authorEmail) {
        User author = authService.getUserByEmail(authorEmail);
        if (author.getRole() != Role.FARMER && author.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("Only registered farmers and agricultural administrators can publish advice and advisories.");
        }

        AgriculturalPost post = AgriculturalPost.builder()
                .author(author)
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .province(request.getProvince() != null ? request.getProvince() : "All Island")
                .targetCrop(request.getTargetCrop() != null ? request.getTargetCrop() : "General Crops")
                .season(request.getSeason() != null ? request.getSeason() : "Year Round")
                .isPinned(Boolean.TRUE.equals(request.getIsPinned()))
                .sourceUrl(request.getSourceUrl())
                .build();

        return mapToResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id, String userEmail) {
        AgriculturalPost post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agricultural post not found with id: " + id));
        User user = authService.getUserByEmail(userEmail);

        if (!post.getAuthor().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("You can only delete your own published posts.");
        }

        postRepository.delete(post);
    }

    @PostConstruct
    public void initSeedData() {
        try {
            if (postRepository.count() == 0) {
                User admin = userRepository.findByEmail("admin@farmora.com").orElse(null);
                if (admin == null) {
                    admin = userRepository.findAll().stream().findFirst().orElse(null);
                }
                if (admin == null) return;

                // 1. Crop Calendar
                postRepository.save(AgriculturalPost.builder()
                        .author(admin)
                        .title("2026/2027 Maha & Yala Crop Calendar Guide for Sri Lanka")
                        .category(AgriCategory.CROP_CALENDAR)
                        .content("Maha season cultivation commences in September/October with the North-East Monsoon, concluding in February/March. Major crops: Paddy (Kurunegala, Anuradhapura, Polonnaruwa), Maize, and highland vegetables. Yala season spans April to August with the South-West Monsoon focusing on short-term pulses, mung beans, cowpea, and irrigated chili.")
                        .province("All Island")
                        .targetCrop("Paddy & Pulses")
                        .season("Maha / Yala")
                        .isPinned(true)
                        .build());

                // 2. Weather Alert
                postRepository.save(AgriculturalPost.builder()
                        .author(admin)
                        .title("Advisory: Inter-Monsoonal Thunderstorms & Vegetable Field Drainage")
                        .category(AgriCategory.WEATHER_ALERT)
                        .content("The Department of Meteorology forecasts active convective clouds and heavy evening thundershowers (above 75mm) across Central, Sabaragamuwa, and Uva provinces. Farmers cultivating carrots, leeks, and cabbage in Nuwara Eliya and Badulla are advised to clear contour drainage furrows immediately to avoid fungal root rot.")
                        .province("Central Province")
                        .targetCrop("Up-Country Vegetables")
                        .season("Inter-Monsoon 2026")
                        .isPinned(true)
                        .build());

                // 3. Government Announcement
                postRepository.save(AgriculturalPost.builder()
                        .author(admin)
                        .title("Department of Agriculture: Bio-Fertilizer Subsidy Scheme 2026")
                        .category(AgriCategory.GOV_ANNOUNCEMENT)
                        .content("Agrarian Services Centers have initiated registration for the 2026 Eco-Agri subsidy. Verified smallholder farmers producing registered organic compost and GAP-certified vegetables are eligible for direct bank grants up to LKR 25,000 per acre. Applications close by October 31.")
                        .province("All Island")
                        .targetCrop("Organic Vegetables & Fruits")
                        .season("Maha 2026/27")
                        .isPinned(false)
                        .sourceUrl("https://doa.gov.lk")
                        .build());

                // 4. Farming Advice
                postRepository.save(AgriculturalPost.builder()
                        .author(admin)
                        .title("Integrated Pest Management (IPM) for Fall Armyworm in Maize")
                        .category(AgriCategory.ADVICE)
                        .content("To combat Spodoptera frugiperda (Sena caterpillar) without heavy chemical residue, deploy pheromone traps early in the vegetative phase. Apply neem kernel aqueous extract (5%) or Bacillus thuringiensis (Bt) sprays during dusk when larvae emerge to feed on whorl tissue.")
                        .province("North Central")
                        .targetCrop("Maize / Corn")
                        .season("Year Round")
                        .isPinned(false)
                        .build());

                // 5. Community Forum Discussion
                postRepository.save(AgriculturalPost.builder()
                        .author(admin)
                        .title("Community Q&A: Solar Drip Irrigation vs. Sprinkler Systems in Dry Zone")
                        .category(AgriCategory.COMMUNITY_FORUM)
                        .content("Farmer discussion topic: Has anyone upgraded their 2-acre plot in Dambulla to solar micro-drip setups? Sharing operational costs, pump head requirements, and water savings during the Yala drought period. Join the community thread and share your tips!")
                        .province("Central Province")
                        .targetCrop("Chili & Onions")
                        .season("Yala Season")
                        .isPinned(false)
                        .build());

                // 6. Farmer Education & Agronomy Masterclass
                postRepository.save(AgriculturalPost.builder()
                        .author(admin)
                        .title("Farmer Masterclass: Organic Soil Regeneration & SL-GAP Certification Walkthrough")
                        .category(AgriCategory.EDUCATION)
                        .content("Comprehensive training guide for local growers: Step-by-step soil pH optimization, nitrogen fixation with leguminous cover crops (sunn hemp), vermicompost preparation ratios, and how to pass the Sri Lanka Good Agricultural Practices (SL-GAP) field audit.")
                        .province("All Island")
                        .targetCrop("All Horticultural Crops")
                        .season("Year Round")
                        .isPinned(false)
                        .sourceUrl("https://doa.gov.lk/gap")
                        .build());
            }
        } catch (Exception e) {
            // Ignore during startup if DB is initializing
        }
    }

    public AgriculturalPostResponse mapToResponse(AgriculturalPost p) {
        return AgriculturalPostResponse.builder()
                .id(p.getId())
                .author(authService.mapToUserResponse(p.getAuthor()))
                .title(p.getTitle())
                .category(p.getCategory())
                .content(p.getContent())
                .province(p.getProvince())
                .targetCrop(p.getTargetCrop())
                .season(p.getSeason())
                .isPinned(p.getIsPinned())
                .sourceUrl(p.getSourceUrl())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
