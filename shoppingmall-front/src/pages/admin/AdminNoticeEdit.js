import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ReactQuill from 'react-quill-new';
import 'react-quill-new/dist/quill.snow.css';
import { fetchWithAuth, uploadImageWithAuth } from '../../utils/api';
import '../../css/admin/ProductForm.css';

const AdminNoticeEdit = () => {
  const { noticeNo } = useParams();
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const quillRef = useRef(null);

  useEffect(() => {
    const fetchNotice = async () => {
      try {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/api/notices/${noticeNo}`);
        if (response.ok) {
          const data = await response.json();
          setTitle(data.title);
          setContent(data.content);
        } else {
          alert('공지사항을 불러오지 못했습니다.');
          navigate('/admin/notices');
        }
      } catch (error) {
        console.error('공지사항 로딩 실패:', error);
      }
    };
    fetchNotice();
  }, [noticeNo, navigate]);

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
        console.error('이미지 업로드 실패:', error);
      }
    });
  };

  const modules = useMemo(() => ({
    toolbar: {
      container: [[{ 'header': [1, 2, false] }], ['bold', 'italic', 'underline', 'strike', 'blockquote'], [{'list': 'ordered'}, {'list': 'bullet'}], ['link', 'image'], ['clean']],
      handlers: { image: imageHandler }
    }
  }), []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) {
      alert('제목과 내용을 모두 입력해주세요.'); return;
    }

    try {
      const response = await fetchWithAuth(`/admin/notices/${noticeNo}`, {
        method: 'PUT',
        body: JSON.stringify({ title, content })
      });

      if (response.ok) {
        alert('공지사항이 수정되었습니다.');
        navigate('/admin/notices');
      } else {
        alert('수정에 실패했습니다.');
      }
    } catch (error) {
      console.error('수정 에러:', error);
    }
  };

  return (
    <div className="admin-layout">
      <div className="admin-content">
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <h2 style={{ marginBottom: '20px', paddingBottom: '15px', borderBottom: '1px solid #333' }}>공지사항 수정</h2>
          
          <form onSubmit={handleSubmit} style={{ backgroundColor: '#fff', padding: '40px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
            
            <div className="form-group" style={{ marginBottom: '25px' }}>
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>공지사항 제목 *</label>
              <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required style={{ width: '100%', padding: '12px', border: '1px solid #ddd', borderRadius: '4px', fontSize: '15px', boxSizing: 'border-box' }} />
            </div>

            <div className="form-group">
              <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>공지사항 내용 *</label>
              <ReactQuill ref={quillRef} modules={modules} theme="snow" value={content} onChange={setContent} style={{ height: '500px', marginBottom: '60px' }} />
            </div>

            <div style={{ textAlign: 'center', marginTop: '20px' }}>
              <button type="button" onClick={() => navigate('/admin/notices')} style={{ padding: '15px 30px', backgroundColor: '#fff', color: '#333', border: '1px solid #ddd', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold', marginRight: '10px' }}>취소</button>
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

export default AdminNoticeEdit;