import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ReactQuill from 'react-quill-new';
import 'react-quill-new/dist/quill.snow.css';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { ko } from 'date-fns/locale/ko'; 
import { fetchWithAuth, uploadImageWithAuth } from '../../utils/api';
import '../../css/admin/ProductForm.css';
import '../../css/admin/AdminCalendar.css'; // 달력 CSS

const AdminEventEdit = () => {
  const { eventNo } = useParams(); // 주소창에서 이벤트 번호 뽑아오기
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  
  const quillRef = useRef(null);
  const fileInputRef = useRef(null);

  // 처음에 기존 이벤트 데이터를 불러와서 화면에 채워주기
  useEffect(() => {
    const fetchEvent = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/api/events/${eventNo}`);
        if (response.ok) {
          const data = await response.json();
          setTitle(data.title);
          setContent(data.content);
          setThumbnailUrl(data.thumbnailUrl || '');
          // 문자열 날짜를 달력이 이해할 수 있는 Date 객체로 변환
          setStartDate(new Date(data.startDate));
          setEndDate(new Date(data.endDate));
        } else {
          alert('이벤트 정보를 불러오지 못했습니다.');
          navigate('/admin/events');
        }
      } catch (error) {
        console.error('이벤트 로딩 실패:', error);
      }
    };
    fetchEvent();
  }, [eventNo, navigate]);

  // 본문 에디터 이미지 업로드
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
          const editor = quillRef.current.getEditor();
          const range = editor.getSelection();
          editor.insertEmbed(range.index, 'image', baseUrl + data.imageUrl);
        }
      } catch (error) {
        console.error('본문 이미지 업로드 실패:', error);
      }
    });
  };

  const modules = useMemo(() => ({
    toolbar: {
      container: [[{ 'header': [1, 2, false] }], ['bold', 'italic', 'underline', 'strike', 'blockquote'], [{'list': 'ordered'}, {'list': 'bullet'}], ['link', 'image'], ['clean']],
      handlers: { image: imageHandler }
    }
  }), []);

  // 썸네일 이미지 업로드
  const handleThumbnailUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('image', file);
    try {
      const response = await uploadImageWithAuth('/admin/products/editor/image', formData);
      if (response.ok) {
        const data = await response.json();
        setThumbnailUrl(data.imageUrl);
      } else {
        alert('이미지 업로드에 실패했습니다.');
      }
    } catch (error) {
      console.error('배너 이미지 업로드 에러:', error);
    }
  };

  const formatForServer = (dateObj) => {
    if(!dateObj) return null;
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:00`;
  };

  // 제출 시 POST가 아니라 PUT으로 요청
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim() || !startDate || !endDate) {
      alert('제목, 내용, 시작일, 종료일은 필수 입력 항목입니다.'); return;
    }
    if (startDate > endDate) {
      alert('종료일이 시작일보다 빠를 수 없습니다.'); return;
    }

    try {
      const response = await fetchWithAuth(`/admin/events/${eventNo}`, {
        method: 'PUT', // 수정 API 호출
        body: JSON.stringify({ 
            title, content, thumbnailUrl, 
            startDate: formatForServer(startDate), 
            endDate: formatForServer(endDate) 
        })
      });

      if (response.ok) {
        alert('이벤트가 성공적으로 수정되었습니다!');
        navigate('/admin/events'); 
      } else {
        alert('이벤트 수정에 실패했습니다.');
      }
    } catch (error) {
      console.error('이벤트 수정 에러:', error);
    }
  };

  return (
    <div className="admin-layout">
      <div className="admin-content">
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          
          <h2 style={{ marginBottom: '20px', paddingBottom: '15px', borderBottom: '1px solid #333' }}>이벤트 수정</h2>
          
          <form onSubmit={handleSubmit} style={{ backgroundColor: '#fff', padding: '40px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
            
            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>이벤트 제목 *</label>
              <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px', boxSizing: 'border-box' }} />
            </div>

            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>이벤트 기간 (날짜선택) *</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '15px', backgroundColor: '#f9f9f9', padding: '15px', borderRadius: '6px', border: '1px solid #eee' }}>
                <div style={{ flex: 1 }}>
                  <DatePicker locale={ko} selected={startDate} onChange={(date) => setStartDate(date)} selectsStart startDate={startDate} endDate={endDate} showTimeInput timeInputLabel="시간:" dateFormat="yyyy.MM.dd HH:mm" customInput={<input style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px', cursor: 'pointer', textAlign: 'center' }} />} />
                </div>
                <span style={{ fontWeight: 'bold', color: '#666' }}>-</span>
                <div style={{ flex: 1 }}>
                  <DatePicker locale={ko} selected={endDate} onChange={(date) => setEndDate(date)} selectsEnd startDate={startDate} endDate={endDate} minDate={startDate} showTimeInput timeInputLabel="시간:" dateFormat="yyyy.MM.dd HH:mm" customInput={<input style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px', cursor: 'pointer', textAlign: 'center' }} />} />
                </div>
              </div>
            </div>

            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>대표 배너 이미지</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                <input type="file" accept="image/*" onChange={handleThumbnailUpload} ref={fileInputRef} style={{ display: 'none' }} />
                <button type="button" onClick={() => fileInputRef.current.click()} style={{ padding: '10px 20px', backgroundColor: '#f8f9fa', color: '#333', border: '1px solid #bbb', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
                  배너 이미지 변경
                </button>
                <span style={{ color: '#666', fontSize: '14px' }}>{thumbnailUrl ? '✅ 이미지가 첨부되었습니다.' : '선택된 파일이 없습니다.'}</span>
              </div>
              {thumbnailUrl && (
                <div style={{ marginTop: '15px' }}>
                  <img src={`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}${thumbnailUrl}`} alt="배너 미리보기" style={{ maxWidth: '400px', border: '1px solid #eee', borderRadius: '4px' }} />
                </div>
              )}
            </div>

            <div className="form-group">
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>이벤트 상세 내용 *</label>
              <ReactQuill ref={quillRef} modules={modules} theme="snow" value={content} onChange={setContent} style={{ height: '500px', marginBottom: '60px' }} />
            </div>

            <div style={{ textAlign: 'center', marginTop: '20px' }}>
              <button type="button" onClick={() => navigate('/admin/events')} style={{ padding: '15px 30px', backgroundColor: '#fff', color: '#333', border: '1px solid #ddd', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold', marginRight: '10px' }}>취소</button>
              <button type="submit" style={{ padding: '15px 50px', backgroundColor: '#1890ff', color: '#fff', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' }}>
                수정 완료
              </button>
            </div>

          </form>
        </div>
      </div>
    </div>
  );
};

export default AdminEventEdit;