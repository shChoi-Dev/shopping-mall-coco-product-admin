import React, { useState, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import ReactQuill from 'react-quill-new';
import 'react-quill-new/dist/quill.snow.css';
import DatePicker from 'react-datepicker'; // 달력 컴포넌트
import 'react-datepicker/dist/react-datepicker.css'; // 달력 기본 CSS
import '../../css/admin/AdminCalendar.css'; // 달력 디자인 불러오기
import { ko } from 'date-fns/locale/ko'; // 한국어 번역팩
import { fetchWithAuth, uploadImageWithAuth } from '../../utils/api';
import '../../css/admin/ProductForm.css';

const AdminEventNew = () => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [thumbnailUrl, setThumbnailUrl] = useState(''); // 배너 이미지 주소

  // 달력 라이브러리 State (초기값을 null로 설정)
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  
  const quillRef = useRef(null);
  const fileInputRef = useRef(null); // 커스텀 파일 버튼을 위한 Ref
  const navigate = useNavigate();

  // 본문(에디터) 이미지 업로드 핸들러 (공지사항과 동일)
  const imageHandler = () => {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();

    input.addEventListener('change', async () => {
      const file = input.files[0];
      const formData = new FormData();
      formData.append('image', file);

      try {
        const response = await uploadImageWithAuth('/admin/products/editor/image', formData);
        if (response.ok) {
          const data = await response.json();
          const baseUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';
          const imageUrl = baseUrl + data.imageUrl;

          const editor = quillRef.current.getEditor();
          const range = editor.getSelection();
          editor.insertEmbed(range.index, 'image', imageUrl);
        }
      } catch (error) {
        console.error('본문 이미지 업로드 실패:', error);
      }
    });
  };

  const modules = useMemo(() => {
    return {
      toolbar: {
        container: [
          [{ 'header': [1, 2, false] }],
          ['bold', 'italic', 'underline', 'strike', 'blockquote'],
          [{'list': 'ordered'}, {'list': 'bullet'}],
          ['link', 'image'],
          ['clean']
        ],
        handlers: { image: imageHandler }
      }
    };
  }, []);

  // 대표 배너(썸네일) 이미지 업로드 핸들러
  const handleThumbnailUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('image', file);

    try {
      // 이미지 업로드 API 사용
      const response = await uploadImageWithAuth('/admin/products/editor/image', formData);
      if (response.ok) {
        const data = await response.json();
        setThumbnailUrl(data.imageUrl); // 백엔드에서 받은 이미지 경로 저장
        alert('배너 이미지가 성공적으로 업로드되었습니다.');
      } else {
        alert('이미지 업로드에 실패했습니다.');
      }
    } catch (error) {
      console.error('배너 이미지 업로드 에러:', error);
    }
  };

  // 자바스크립트 Date 객체를 스프링부트용(YYYY-MM-DDTHH:mm:ss) 문자로 변환
  const formatForServer = (dateObj) => {
    if(!dateObj) return null;
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:00`;
  };

  // 작성 완료 버튼 클릭 시 DB 저장
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim() || !startDate || !endDate) {
      alert('제목, 내용, 시작일, 종료일은 필수 입력 항목입니다.');
      return;
    }

    if (startDate > endDate) {
      alert('종료일이 시작일보다 빠를 수 없습니다.');
      return;
    }

    try {
      const response = await fetchWithAuth('/admin/events', {
        method: 'POST',
        body: JSON.stringify({ 
            title, 
            content, 
            thumbnailUrl, 
            startDate: formatForServer(startDate), 
            endDate: formatForServer(endDate)
        })
      });

      if (response.ok) {
        alert('이벤트가 성공적으로 등록되었습니다!');
        navigate('/admin/events'); // 저장 후 이벤트 목록 페이지로 이동
      } else {
        alert('이벤트 등록에 실패했습니다.');
      }
    } catch (error) {
      console.error('이벤트 등록 에러:', error);
    }
  };

  return (
    <div className="admin-layout">

        <style>{`.react-datepicker-wrapper { width: 100%; }`}</style>

      <div className="admin-content">
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          
          <h2 style={{ marginBottom: '20px', paddingBottom: '15px', borderBottom: '1px solid #333' }}>이벤트 등록</h2>
          
          <form onSubmit={handleSubmit} style={{ backgroundColor: '#fff', padding: '40px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
            
            {/* 제목 입력 */}
            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>이벤트 제목 *</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="진행할 이벤트 제목을 입력하세요"
                required
                style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px' }}
              />
            </div>

            {/* 이벤트 기간 선택 (KRDS 스타일의 달력 UI) */}
            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>이벤트 기간 (날짜선택) *</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '15px', backgroundColor: '#f9f9f9', padding: '15px', borderRadius: '6px', border: '1px solid #eee' }}>
                
                <div style={{ flex: 1 }}>
                  <DatePicker
                    locale={ko} // 한국어 패치
                    selected={startDate}
                    onChange={(date) => setStartDate(date)}
                    selectsStart
                    startDate={startDate}
                    endDate={endDate}
                    showTimeInput // 30분 단위가 아닌, 키보드로 1분 단위 직접 입력 가능!
                    timeInputLabel="시간:"
                    dateFormat="yyyy.MM.dd HH:mm"
                    placeholderText="시작날짜 입력"
                    customInput={<input style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px', cursor: 'pointer', textAlign: 'center' }} />}
                  />
                </div>
                
                <span style={{ fontWeight: 'bold', color: '#666' }}>-</span>
                
                <div style={{ flex: 1 }}>
                  <DatePicker
                    locale={ko} // 한국어 패치
                    selected={endDate}
                    onChange={(date) => setEndDate(date)}
                    selectsEnd
                    startDate={startDate}
                    endDate={endDate}
                    minDate={startDate}
                    showTimeInput // 1분 단위 직접 입력 가능!
                    timeInputLabel="시간:"
                    dateFormat="yyyy.MM.dd HH:mm"
                    placeholderText="종료날짜 입력"
                    customInput={<input style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px', cursor: 'pointer', textAlign: 'center' }} />}
                  />
                </div>

              </div>
            </div>

            {/* 썸네일(배너) 이미지 업로드 */}
            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>대표 배너 이미지</label>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleThumbnailUpload}
                  ref={fileInputRef}
                  style={{ display: 'none' }} // 진짜 input은 안 보이게 숨김
                />
                <button
                  type="button"
                  onClick={() => fileInputRef.current.click()} // 버튼을 누르면 숨겨진 input이 눌린 것처럼 표시
                  style={{ padding: '10px 20px', backgroundColor: '#f8f9fa', color: '#333', border: '1px solid #bbb', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                >
                  배너 이미지 선택
                </button>
                <span style={{ color: '#666', fontSize: '14px' }}>
                  {thumbnailUrl ? '✅ 이미지가 첨부되었습니다.' : '선택된 파일이 없습니다.'}
                </span>
              </div>

              {thumbnailUrl && (
                <div style={{ marginTop: '15px' }}>
                  <img 
                    src={`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}${thumbnailUrl}`} 
                    alt="배너 미리보기" 
                    style={{ maxWidth: '400px', border: '1px solid #eee', borderRadius: '4px' }} 
                  />
                </div>
              )}
            </div>

            {/* 에디터 (본문) */}
            <div className="form-group">
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>이벤트 상세 내용 *</label>
              <ReactQuill
                ref={quillRef}
                modules={modules}
                theme="snow"
                value={content}
                onChange={setContent}
                style={{ height: '500px', marginBottom: '60px' }}
              />
            </div>

            {/* 제출 버튼 */}
            <div style={{ textAlign: 'center', marginTop: '20px' }}>
              <button 
                type="submit" 
                style={{ padding: '15px 50px', backgroundColor: '#000', color: '#fff', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}
              >
                이벤트 등록하기
              </button>
            </div>

          </form>
        </div>
      </div>
    </div>
  );
};

export default AdminEventNew;