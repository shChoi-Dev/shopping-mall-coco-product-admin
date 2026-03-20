import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

const NoticeDetailPage = () => {
    const { noticeNo } = useParams(); // 주소창에서 공지사항 번호(noticeNo)를 가져옴
    const navigate = useNavigate();
    const [notice, setNotice] = useState(null);

    useEffect(() => {
        const fetchNoticeDetail = async () => {
            try {
                const response = await fetch(`${process.env.REACT_APP_API_URL}/api/notices/${noticeNo}`);
                if (response.ok) {
                    const data = await response.json();
                    setNotice(data);
                } else {
                    alert('존재하지 않거나 삭제된 공지사항입니다.');
                    navigate('/notices'); // 실패 시 목록으로 돌아감
                }
            } catch (error) {
                console.error('공지사항 상세 로딩 실패:', error);
            }
        };

        fetchNoticeDetail();
    }, [noticeNo, navigate]);

    if (!notice) return <div style={{ textAlign: 'center', padding: '100px' }}> 로딩 중... </div>;

    return (
        <div style={{ maxWidth: '800px', margin: '0 auto', padding: '50px 20px', minHeight: '600px' }}>

            {/* 공지사항 타이틀 및 메타 정보 */}
            <h2 style={{ textAlign: 'center', marginBottom: '40px', fontWeight: 'bold' }}>공지사항</h2>

            <div style={{ borderBottom: '2px solid #000', paddingBottom: '20px', marginBottom: '30px' }}>
                <h3 style={{ fontSize: '24px', margin: '0 0 15px 0' }}>{notice.title}</h3>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: '#666', fontSize: '14px' }}>
                    <span>작성일 : {new Date(notice.createdAt).toLocaleDateString()}</span>
                    <span>조회수 : {notice.viewCount}</span>
                </div>
            </div>

            {/* 공지사항 본문 */}
            <div
                style={{ fontSize: '16px', lineHeight: '1.8', paddingBottom: '50px', borderBottom: '1px solid #eee' }}
                dangerouslySetInnerHTML={{ __html: notice.content }}
            />

            {/* 하단 목록 버튼 */}
            <div style={{ textAlign: 'center', marginTop: '40px' }}>
                <button
                    onClick={() => navigate('/notices')}
                    style={{ padding: '12px 40px', backgroundColor: '#000', color: '#fff', border: 'none', fontSize: '16px', cursor: 'pointer' }}
                >
                    목록으로
                </button>
            </div>
        </div>
    );
};

export default NoticeDetailPage;