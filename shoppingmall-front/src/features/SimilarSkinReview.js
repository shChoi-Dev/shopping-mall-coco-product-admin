import { useEffect, useState } from "react";
import { getStoredMemberId, getAuthHeaders } from "../utils/api";
import '../css/Similar.css';
function SimilarSkinReview({ productId }) {
    const [skinStats, setSkinStats] = useState(null);
    const [error, setError] = useState(false);


    // 피부 타입 
    useEffect(() => {
        const fetchSkinStats = async () => {
            // 회원 정보 아이디
            const memberNo = await getStoredMemberId();
            if (!productId || !memberNo) return;

            try {
                const response = await fetch(`http://localhost:8080/api/products/${productId}/similar-skin-tags?memberNo=${memberNo}`, {
                    headers: getAuthHeaders(),
                });
                if (!response.ok) throw new Error('통계 데이터 불러오기 실패');
                const stats = await response.json();
                setSkinStats(stats);
                setError(false);
            } catch (e) {
                console.error('통계 데이터 로딩 에러:', e);
                setSkinStats(null);
                setError(true);
            }
        };
        fetchSkinStats();
    }, [productId]);
    if (error) return <div>통계 데이터를 불러올 수 없습니다.</div>;
    if (skinStats === null) return <div>로딩 중...</div>;

    const positiveCount = (skinStats.topTags || []).filter(tag => tag.tagStatus === '장점').length;
    const negativeCount = (skinStats.topTags || []).filter(tag => tag.tagStatus === '단점').length;
    let boxClass = "similar-review-box";
    if (positiveCount >= 2) boxClass += " positive-box";
    else if (negativeCount >= 2) boxClass += " negative-box";

    return (
        <div className={boxClass}>
            <h3 className="similar-review-title">
                🧴 <strong>{skinStats.skinType}</strong> 타입 고객님들의 선택!
            </h3>
            {Array.isArray(skinStats.topTags) && skinStats.topTags.length > 0 ? (
                <>
                    <p className="similar-review-text">
                        {skinStats.skinType} 타입 사용자 <strong>{skinStats.topTags[0].percentage}%</strong>가{' '}
                        <span className={
                            "similar-tag-chip " +
                            (skinStats.topTags[0].tagStatus === "POSITIVE"
                                ? "positive"
                                : skinStats.topTags[0].tagStatus === "NEGATIVE"
                                    ? "negative"
                                    : "")
                        }> {skinStats.topTags[0].tagName}</span> 태그를 선택했어요.
                    </p>
                    <div className="similar-tag-chips">
                        {skinStats.topTags.map(tag => (
                            <span key={tag.tagName} className={
                                "similar-tag-chip " +
                                (tag.tagStatus === "POSITIVE" ? "positive" : tag.tagStatus === "NEGATIVE" ? "negative" : "")
                            }>{tag.tagName} ({tag.percentage}%)</span>
                        ))}
                    </div>
                </>
            ) : (
                <p className="similar-nodata-text">
                    아직 {skinStats.skinType}타입에 대한 리뷰/태그 데이터가 없습니다.
                </p>
            )}
        </div>
    );
}

export default SimilarSkinReview