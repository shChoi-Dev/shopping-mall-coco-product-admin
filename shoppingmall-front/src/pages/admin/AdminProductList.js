import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom'; // useSearchParams 추가
import axios from 'axios';
import { toast } from 'react-toastify';
import Pagination from '../../components/admin/Pagination';
import Spinner from '../../components/admin/Spinner';
import '../../css/admin/AdminProductList.css';
import editIcon from '../../images/edit.svg';
import deleteIcon from '../../images/delete.svg';

/**
 * [AdminProductList] 관리자용 상품 관리 페이지
 * 역할:
 * 1. 전체 상품 목록 조회 (페이징, 검색, 카테고리/상태 필터)
 * 2. 상품 대시보드 통계 표시 (전체, 판매중, 품절, 재고 현황)
 * 3. 상품 삭제(논리적 삭제) 및 수정 페이지 이동 기능 제공
 * 4. 페이지 상태 유지를 위해 URL SearchParams 사용 / 변경사항
 */

const LIMIT = 6;

function AdminProductList() {
  // URL 파라미터 훅 사용
  const [searchParams, setSearchParams] = useSearchParams();

  // URL에서 상태값 추출 (없으면 기본값 사용)
  const currentPage = parseInt(searchParams.get('page')) || 1;
  const selectedCategory = searchParams.get('category') || '';
  const selectedStatus = searchParams.get('status') || 'ALL';
  const sortOrder = searchParams.get('sort') || 'idAsc';
  const currentQuery = searchParams.get('q') || '';

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);

  // 검색 입력창용 로컬 상태 (URL과 별개로 입력 반응성 위해 필요)
  const [searchTerm, setSearchTerm] = useState(currentQuery);
  const [debounceTimer, setDebounceTimer] = useState(null);

  // 대시보드 상태
  const [dashboardCounts, setDashboardCounts] = useState({
    totalProducts: 0,
    inStock: 0,
    outOfStock: 0,
    totalStock: 0
  });

  // 카테고리 로드
  useEffect(() => {
    axios.get('http://localhost:8080/api/categories')
      .then(res => setCategories(res.data))
      .catch(err => console.error("카테고리 로드 실패:", err));
  }, []);

  // URL 파라미터가 변경될 때마다 데이터 다시 로드
  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      try {
        // 쿼리 파라미터 생성 (URL 값 사용)
        const params = {
          page: currentPage,
          size: LIMIT,
          sort: sortOrder,
          q: currentQuery || undefined,
          categoryNo: selectedCategory || undefined,
          status: selectedStatus || undefined
        };

        const token = localStorage.getItem('token');

        // 상품 목록 요청
        const productRes = await axios.get('http://localhost:8080/api/products', { params });
        setProducts(productRes.data.content);
        setTotalPages(productRes.data.totalPages);

        // 통계 요청
        const statsRes = await axios.get('http://localhost:8080/api/admin/stats', {
          headers: { Authorization: `Bearer ${token}` }
        });

        setDashboardCounts({
          totalProducts: statsRes.data.totalProducts,
          inStock: statsRes.data.inStockProducts,
          outOfStock: statsRes.data.outOfStockProducts,
          totalStock: statsRes.data.totalStock
        });

      } catch (error) {
        console.error("데이터 로드 실패:", error);
        toast.error("데이터를 불러오는 데 실패했습니다.");
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [currentPage, currentQuery, selectedCategory, selectedStatus, sortOrder]);

  // 검색어 입력 시 URL이 아닌 로컬 상태만 업데이트하고 디바운싱 처리
  useEffect(() => {
    setSearchTerm(currentQuery);
  }, [currentQuery]);

  /**
   * 공통 URL 업데이트 함수
   * 기존 파라미터를 유지하면서 변경된 값만 덮어씌움
   */
  const updateParams = (newParams) => {
    const currentParams = Object.fromEntries(searchParams);
    setSearchParams({ ...currentParams, ...newParams });
  };

  /**
   * 상품 삭제 핸들러
   */
  const handleDelete = async (product) => {
    if (window.confirm(`정말 삭제하시겠습니까?\n상품명: ${product.prdName}`)) {
      try {
        const token = localStorage.getItem('token');
        await axios.delete(`http://localhost:8080/api/admin/products/${product.prdNo}`, {
          headers: { Authorization: `Bearer ${token}` }
        });

        toast.success('삭제되었습니다.');

        // 삭제 후 페이지 처리
        if (products.length === 1 && currentPage > 1) {
          // 마지막 아이템 삭제 시 이전 페이지로 이동
          updateParams({ page: currentPage - 1 });
        } else {
          // 현재 페이지 새로고침 효과 (데이터만 다시 로드하거나 상태 필터링)
          setProducts(prev => prev.filter(p => p.prdNo !== product.prdNo));
          setDashboardCounts(prev => ({ ...prev, totalProducts: prev.totalProducts - 1 }));
        }
      } catch (error) {
        toast.error("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  const handleSearchChange = (e) => {
    const value = e.target.value;
    setSearchTerm(value);

    if (debounceTimer) clearTimeout(debounceTimer);

    const newTimer = setTimeout(() => {
      // 검색 시 1페이지로 초기화하며 URL 업데이트
      updateParams({ q: value, page: 1 });
    }, 500);
    setDebounceTimer(newTimer);
  };

  const handleCategoryChange = (e) => {
    updateParams({ category: e.target.value, page: 1 });
  };

  const handleStatusChange = (e) => {
    updateParams({ status: e.target.value, page: 1 });
  };

  const handleSortChange = (e) => {
    updateParams({ sort: e.target.value, page: 1 });
  };

  const handlePageChange = (page) => {
    updateParams({ page: page });
  };

  return (
    <div className="admin-page-container">
      <h2 className="page-title">상품 관리</h2>

      {/* 대시보드 영역 */}
      <div className="dashboard-container">
        <div className="dash-card">
          <p className="dash-title">전체 상품</p>
          <p className="dash-value">{dashboardCounts.totalProducts}개</p>
        </div>
        <div className="dash-card">
          <p className="dash-title">판매중</p>
          <p className="dash-value">{dashboardCounts.inStock}개</p>
        </div>
        <div className="dash-card">
          <p className="dash-title">품절</p>
          <p className="dash-value">{dashboardCounts.outOfStock}개</p>
        </div>
        <div className="dash-card">
          <p className="dash-title">총 재고</p>
          <p className="dash-value">{dashboardCounts.totalStock}개</p>
        </div>
      </div>

      <div className="admin-content-card">
        <div className="content-header">
          <h3>상품 목록</h3>
          <div className="header-actions">
            <button className="btn-refresh" onClick={() => window.location.reload()}>🔄 새로고침</button>
            <Link to="/admin/product/new" className="btn-add-product">+ 상품 등록</Link>
          </div>
        </div>

        {/* 필터 영역 */}
        <div className="filter-container">
          <input
            type="text"
            className="search-input"
            placeholder="상품명 검색..."
            value={searchTerm}
            onChange={handleSearchChange}
          />

          <select className="filter-select" value={selectedCategory} onChange={handleCategoryChange}>
            <option value="">전체 카테고리</option>
            {categories.filter(c => !c.parentCategoryNo).map(cat => (
              <React.Fragment key={cat.categoryNo}>
                <option value={cat.categoryNo}>{cat.categoryName}</option>
                {categories.filter(sub => sub.parentCategoryNo === cat.categoryNo).map(sub => (
                  <option key={sub.categoryNo} value={sub.categoryNo}>&nbsp;&nbsp;└ {sub.categoryName}</option>
                ))}
              </React.Fragment>
            ))}
          </select>

          <select className="filter-select" value={selectedStatus} onChange={handleStatusChange}>
            <option value="ALL">전체 상태</option>
            <option value="SALE">판매중</option>
            <option value="SOLD_OUT">품절</option>
            <option value="STOP">판매중지</option>
          </select>

          <select className="filter-select" value={sortOrder} onChange={handleSortChange}>
            <option value="idAsc">등록순 (ID)</option>
            <option value="newest">최신순</option>
            <option value="popularity">인기순</option>
            <option value="priceAsc">낮은 가격순</option>
            <option value="priceDesc">높은 가격순</option>
          </select>
        </div>

        {/* 테이블 영역 */}
        <div className="table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th style={{ width: '60px' }}>ID</th>
                <th style={{ width: '80px' }}>이미지</th>
                <th>상품명</th>
                <th style={{ width: '120px' }}>카테고리</th>
                <th style={{ width: '100px' }}>가격</th>
                <th style={{ width: '80px' }}>재고</th>
                <th style={{ width: '80px' }}>상태</th>
                <th style={{ width: '120px' }}>관리</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan="8" className="loading-cell"><Spinner /></td></tr>
              ) : products.length > 0 ? (
                products.map((product) => (
                  <tr key={product.prdNo}>
                    <td>{product.prdNo}</td>
                    <td>
                      <img
                        src={product.imageUrl || '/prd_placeholder.png'}
                        alt="상품"
                        className="product-thumb"
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src = '/prd_placeholder.png';
                        }}
                      />
                    </td>
                    <td className="fw-bold">{product.prdName}</td>
                    <td>{product.categoryName}</td>
                    <td>{product.prdPrice.toLocaleString()}원</td>
                    <td>{product.stock}개</td>
                    <td>
                      <span className={`status-tag ${product.status === '판매중' ? 'status-sale' :
                        product.status === '품절' ? 'status-soldout' : 'status-stop'
                        }`}>
                        {product.status}
                      </span>
                    </td>
                    <td>
                      {/* 수정 버튼 링크는 페이지 이동을 하므로, 돌아올 때 URL 파라미터가 유지됨 */}
                      <Link to={`/admin/product/edit/${product.prdNo}`} className="icon-btn edit" title="수정">
                        <img src={editIcon} alt="수정" />
                      </Link>
                      <button onClick={() => handleDelete(product)} className="icon-btn delete" title="삭제">
                        <img src={deleteIcon} alt="삭제" />
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr><td colSpan="8" className="empty-cell">검색 결과가 없습니다.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
        />
      </div>
    </div>
  );
}

export default AdminProductList;