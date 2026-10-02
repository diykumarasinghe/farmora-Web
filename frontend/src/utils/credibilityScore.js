export function calculateCredibility(farm, farmer, orders = []) {
  const isVerified = farm?.verificationStatus === 'VERIFIED'
  const isPending = farm?.verificationStatus === 'PENDING_VERIFICATION'

  // 1. Verification (Max 30)
  let verificationScore = 5
  if (isVerified) {
    verificationScore = 30
  } else if (isPending) {
    verificationScore = 15
  }

  // 2. Experience (Max 25)
  const expYears = farm?.farmingExperience || 0
  const experienceScore = Math.min(25, Math.max(5, Math.round(expYears * 2.5)))

  // 3. Buyer Rating (Max 25)
  const rating = farm?.averageRating || 0
  const reviewsCount = farm?.totalReviews || 0
  let ratingScore = 15 // baseline
  if (reviewsCount > 0) {
    ratingScore = Math.min(25, Math.round((rating / 5.0) * 25))
  } else if (isVerified) {
    ratingScore = 20
  }

  // 4. Completed Transactions (Max 20)
  const deliveredOrders = Array.isArray(orders)
    ? orders.filter(o => o.status === 'DELIVERED').length
    : 0
  const transactionScore = Math.min(20, Math.max(5, deliveredOrders * 4))

  const totalScore = Math.min(100, verificationScore + experienceScore + ratingScore + transactionScore)

  let grade = 'Excellent'
  if (totalScore < 50) grade = 'Building Trust'
  else if (totalScore < 70) grade = 'Fair'
  else if (totalScore < 80) grade = 'Good'

  // Parse certifications
  let certCount = 0
  if (farm?.certifications) {
    try {
      const parsed = JSON.parse(farm.certifications)
      certCount = Array.isArray(parsed) ? parsed.length : 0
    } catch {
      certCount = 0
    }
  }

  // Profile completion checklist (6 sections)
  const sections = [
    { id: 'name', label: 'Farm name', completed: Boolean(farm?.farmName && farm.farmName.trim().length > 0) },
    { id: 'about', label: 'About your farm', completed: Boolean(farm?.farmerDetails && farm.farmerDetails.trim().length > 0) },
    { id: 'location', label: 'Farm location', completed: Boolean(farm?.district && (farm?.farmAddress || farm?.latitude)) },
    { id: 'experience', label: 'Farming experience', completed: Boolean(farm?.farmingExperience && farm.farmingExperience > 0) },
    { id: 'photo', label: 'Add profile photo', completed: Boolean(farmer?.profileImageUrl && farmer.profileImageUrl.trim().length > 0) },
    { id: 'cert', label: 'Add certification', completed: certCount > 0 }
  ]

  const completedCount = sections.filter(s => s.completed).length
  const completionPercentage = Math.round((completedCount / sections.length) * 100)

  return {
    verificationScore,
    experienceScore,
    ratingScore,
    transactionScore,
    totalScore,
    grade,
    certCount,
    sections,
    completedCount,
    totalSections: sections.length,
    completionPercentage
  }
}
