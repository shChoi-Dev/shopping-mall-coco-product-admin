import React, { useState, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import ReactQuill from 'react-quill-new';
import 'react-quill-new/dist/quill.snow.css';
import { fetchWithAuth, uploadImageWithAuth } from '../../utils/api';
import '../../css/admin/ProductForm.css'; 

const AdminNoticeNew = () => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const quillRef = useRef(null);
  const navigate = useNavigate();

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
        console.error('이미지 업로드 실패:', error);
        alert('이미지 업로드에 실패했습니다.');
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

  // 💡 작성 완료 버튼을 눌렀을 때 백엔드로 데이터 전송
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) {
      alert('제목과 내용을 모두 입력해주세요.');
      return;
    }

    try {
      const response = await fetchWithAuth('/admin/notices', {
        method: 'POST',
        body: JSON.stringify({ title, content })
      });

      if (response.ok) {
        alert('공지사항이 성공적으로 등록되었습니다!');
        // 등록 성공 시 목록 페이지로 이동 (목록 페이지는 다음 스텝에서 만들게요!)
        navigate('/admin/notices'); 
      } else {
        alert('등록에 실패했습니다.');
      }
    } catch (error) {
      console.error(error);
      alert('서버 통신 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="admin-layout">
      <div className="admin-content">
        <h2 style={{ marginBottom: '20px' }}>공지사항 등록</h2>
        <form onSubmit={handleSubmit} style={{ backgroundColor: 'white', padding: '30px', borderRadius: '8px' }}>
          
          <div className="form-group" style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>제목</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="공지사항 제목을 입력하세요"
              required
              style={{ width: '100%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
            />
          </div>

          <div className="form-group">
            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '10px' }}>내용</label>
            <ReactQuill
              ref={quillRef}
              modules={modules}
              theme="snow"
              value={content}
              onChange={setContent}
              style={{ height: '400px', marginBottom: '50px' }}
            />
          </div>

          <div style={{ textAlign: 'center' }}>
            <button 
                type="submit" 
                style={{ padding: '10px 30px', backgroundColor: '#000', color: '#fff', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px' }}
            >
                등록하기
            </button>
          </div>

        </form>
      </div>
    </div>
  );
};

export default AdminNoticeNew;