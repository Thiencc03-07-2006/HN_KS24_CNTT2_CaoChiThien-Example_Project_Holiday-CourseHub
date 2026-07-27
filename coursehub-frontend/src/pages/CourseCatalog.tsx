import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, Tag, message, Breadcrumb, TreeSelect } from 'antd';
import { SearchOutlined, StarFilled, BookOutlined, FilterOutlined, HeartFilled, HeartOutlined, FlagFilled, FlagOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { courseService } from '../services/courseService';
import { wishlistService } from '../services/wishlistService';
import { favoriteService } from '../services/favoriteService';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';

// Import themed UI and layout components
import { Header } from '../components/common/Layout/Header';
import { Footer } from '../components/common/Layout/Footer';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';
import { Select } from '../components/common/UI/Select';
import { Avatar } from '../components/common/UI/Avatar';
import { Loading } from '../components/common/UI/Loading';
import { EmptyState } from '../components/common/UI/EmptyState';
import { Pagination } from '../components/common/UI/Pagination';

const { Title, Paragraph, Text } = Typography;
const { Option } = Select;

interface Course {
  id: string;
  title: string;
  slug: string;
  shortDescription: string;
  price: number;
  thumbnailUrl?: string;
  level: string;
  averageRating: number;
  totalReviews: number;
  totalStudents: number;
  instructor: {
    fullName: string;
    avatarUrl?: string;
  };
  category: {
    name: string;
  };
}

interface Category {
  id: number;
  name: string;
}

const LEVEL_LABELS: Record<string, { label: string; color: string }> = {
  BEGINNER: { label: 'Cơ bản', color: 'green' },
  INTERMEDIATE: { label: 'Trung cấp', color: 'blue' },
  ADVANCED: { label: 'Nâng cao', color: 'purple' },
};

const CourseCatalog: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [courses, setCourses] = useState<Course[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [wishlistIds, setWishlistIds] = useState<Set<string>>(new Set());
  const [favoriteIds, setFavoriteIds] = useState<Set<string>>(new Set());

  const [page, setPage] = useState(0);
  const [pageSize] = useState(12);
  const [total, setTotal] = useState(0);

  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [level, setLevel] = useState<string | undefined>(undefined);
  const [sortBy, setSortBy] = useState('newest');

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await axiosInstance.get('/categories');
        if (res.data?.success && res.data?.data) {
          setCategories(res.data.data);
        }
      } catch (err) {
        console.warn('Failed to load categories');
      }
    };
    fetchCategories();
  }, []);

  useEffect(() => {
    const fetchWishlist = async () => {
      if (!user) {
        setWishlistIds(new Set());
        return;
      }
      try {
        const res = await wishlistService.getMyWishlist();
        if (res?.success && res?.data) {
          const ids = new Set<string>(res.data.map((c: any) => c.id));
          setWishlistIds(ids);
        }
      } catch (err) {
        console.warn('Failed to load wishlist');
      }
    };

    const fetchFavorites = async () => {
      if (!user) {
        setFavoriteIds(new Set());
        return;
      }
      try {
        const res = await favoriteService.getMyFavorites();
        if (res?.success && res?.data) {
          const ids = new Set<string>(res.data.map((c: any) => c.id));
          setFavoriteIds(ids);
        }
      } catch (err) {
        console.warn('Failed to load favorites');
      }
    };

    fetchWishlist();
    fetchFavorites();
  }, [user]);

  const handleWishlistToggle = async (courseId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!user) {
      message.info('Vui lòng đăng nhập để sử dụng chức năng yêu thích.');
      navigate('/login');
      return;
    }

    const isWishlisted = wishlistIds.has(courseId);
    try {
      if (isWishlisted) {
        await wishlistService.removeFromWishlist(courseId);
        const updated = new Set(wishlistIds);
        updated.delete(courseId);
        setWishlistIds(updated);
        message.success('Đã xóa khỏi danh sách mong muốn.');
      } else {
        await wishlistService.addToWishlist(courseId);
        const updated = new Set(wishlistIds);
        updated.add(courseId);
        setWishlistIds(updated);
        message.success('Đã thêm vào danh sách mong muốn.');
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật danh sách mong muốn.');
    }
  };

  const handleFavoriteToggle = async (courseId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!user) {
      message.info('Vui lòng đăng nhập để sử dụng chức năng yêu thích.');
      navigate('/login');
      return;
    }

    const isFavorited = favoriteIds.has(courseId);
    try {
      if (isFavorited) {
        await favoriteService.removeFavorite(courseId);
        const updated = new Set(favoriteIds);
        updated.delete(courseId);
        setFavoriteIds(updated);
        message.success('Đã xóa khỏi danh sách yêu thích.');
      } else {
        await favoriteService.addFavorite(courseId);
        const updated = new Set(favoriteIds);
        updated.add(courseId);
        setFavoriteIds(updated);
        message.success('Đã thêm vào danh sách yêu thích.');
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật danh sách yêu thích.');
    }
  };

  const fetchCourses = async () => {
    setLoading(true);
    try {
      const data = await courseService.searchCourses({
        page,
        size: pageSize,
        sortBy,
        keyword: keyword || undefined,
        categoryId: categoryId || undefined,
        level: level || undefined,
      });
      if (data?.success && data?.data) {
        setCourses(data.data.content || []);
        setTotal(data.data.totalElements || 0);
      }
    } catch (err) {
      message.error('Không thể tải danh sách khóa học.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, [page, pageSize, categoryId, level, sortBy]);
  const handleSearch = () => {
    setPage(0);
    fetchCourses();
  };

  const buildTreeData = (items: any[]): any[] => {
    return items.map(item => ({
      title: item.name,
      value: item.id,
      key: item.id,
      children: item.children && item.children.length > 0 ? buildTreeData(item.children) : undefined
    }));
  };

  return (
    <div
      style={{
        background: 'var(--bg-primary)',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        width: '100%',
        maxWidth: '100%',
        overflowX: 'hidden',
      }}
    >
      <Header />

      {/* Hero Banner */}
      <div
        style={{
          background: 'linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%)',
          padding: '48px 24px 40px',
          borderBottom: '1px solid var(--border-color)',
          textAlign: 'center',
        }}
      >
        <div style={{ maxWidth: 720, margin: '0 auto' }}>
          <Tag color="blue" style={{ marginBottom: 16, fontSize: 12, fontWeight: 600, padding: '4px 12px', borderRadius: 20 }}>
            <BookOutlined /> Hơn {total > 0 ? total : '100'}+ khóa học
          </Tag>
          <Title
            style={{
              color: 'var(--text-h)',
              fontSize: 'clamp(24px, 4vw, 40px)',
              fontWeight: 800,
              margin: '0 0 16px',
              lineHeight: 1.2,
            }}
          >
            Khám phá các Khóa học chất lượng cao
          </Title>
          <Paragraph
            style={{
              color: 'var(--text-muted)',
              fontSize: 16,
              marginBottom: 0,
              lineHeight: 1.6,
            }}
          >
            Học lập trình, kinh doanh, thiết kế từ các chuyên gia hàng đầu. Học mọi lúc, mọi nơi.
          </Paragraph>
        </div>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, padding: '32px 24px', boxSizing: 'border-box' }}>
        <div style={{ maxWidth: 1400, margin: '0 auto', width: '100%' }}>

          {/* Breadcrumb */}
          <Breadcrumb
            style={{ marginBottom: 24 }}
            items={[
              { title: <Link to="/">Trang chủ</Link> },
              { title: 'Khóa học' },
            ]}
          />

          {/* Search and Filters */}
          <div
            style={{
              background: '#FFFFFF',
              borderRadius: '12px',
              border: '1px solid var(--border-color)',
              boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
              padding: '20px 24px',
              marginBottom: 28,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
              <FilterOutlined style={{ color: 'var(--primary)' }} />
              <Text strong style={{ color: 'var(--text-h)', fontSize: 14 }}>Bộ lọc tìm kiếm</Text>
            </div>
            <Row gutter={[12, 12]} align="middle">
              <Col xs={24} md={9}>
                <Input
                  placeholder="Tìm kiếm tên khóa học..."
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  onPressEnter={handleSearch}
                  suffix={
                    <SearchOutlined
                      style={{ color: 'var(--primary)', cursor: 'pointer' }}
                      onClick={handleSearch}
                    />
                  }
                  size="large"
                  style={{ borderRadius: 8 }}
                />
              </Col>
              <Col xs={24} sm={8} md={5}>
                <TreeSelect
                  placeholder="Danh mục"
                  allowClear
                  style={{ width: '100%' }}
                  size="large"
                  treeData={buildTreeData(categories)}
                  treeDefaultExpandAll
                  value={categoryId}
                  onChange={(val) => { setCategoryId(val as number); setPage(0); }}
                />
              </Col>
              <Col xs={24} sm={8} md={5}>
                <Select
                  placeholder="Cấp độ"
                  allowClear
                  style={{ width: '100%' }}
                  size="large"
                  onChange={(val) => { setLevel(val as string); setPage(0); }}
                >
                  <Option value="BEGINNER">Cơ bản</Option>
                  <Option value="INTERMEDIATE">Trung cấp</Option>
                  <Option value="ADVANCED">Nâng cao</Option>
                </Select>
              </Col>
              <Col xs={24} sm={8} md={5}>
                <Select
                  defaultValue="newest"
                  style={{ width: '100%' }}
                  size="large"
                  onChange={(val) => { setSortBy(val as string); setPage(0); }}
                >
                  <Option value="newest">Mới nhất</Option>
                  <Option value="price_asc">Giá: Thấp → Cao</Option>
                  <Option value="price_desc">Giá: Cao → Thấp</Option>
                  <Option value="rating">Đánh giá tốt nhất</Option>
                </Select>
              </Col>
            </Row>
          </div>

          {/* Results summary */}
          {!loading && (
            <div style={{ marginBottom: 20, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Text style={{ color: 'var(--text-muted)', fontSize: 14 }}>
                Tìm thấy <Text strong style={{ color: 'var(--text-h)' }}>{total}</Text> khóa học
              </Text>
            </div>
          )}

          {/* Courses List */}
          {loading ? (
            <Loading message="Đang tải danh sách khóa học..." />
          ) : courses.length === 0 ? (
            <div style={{ background: '#FFFFFF', borderRadius: 16, padding: '60px 24px', textAlign: 'center', border: '1px solid var(--border-color)' }}>
              <EmptyState description="Không tìm thấy khóa học nào phù hợp. Hãy thử từ khóa khác." />
            </div>
          ) : (
            <>
              <Row gutter={[20, 20]}>
                {courses.map((course) => {
                  const levelInfo = LEVEL_LABELS[course.level] || { label: course.level, color: 'default' };
                  return (
                    <Col xs={24} sm={12} lg={8} xl={6} key={course.id}>
                      <div
                        style={{
                          background: '#FFFFFF',
                          borderRadius: 16,
                          border: '1px solid var(--border-color)',
                          boxShadow: '0 4px 12px rgba(0,0,0,0.06)',
                          overflow: 'hidden',
                          display: 'flex',
                          flexDirection: 'column',
                          height: '100%',
                          cursor: 'pointer',
                          transition: 'all 0.2s ease',
                        }}
                        onClick={() => navigate(`/courses/${course.slug}`)}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.transform = 'translateY(-4px)';
                          e.currentTarget.style.boxShadow = '0 12px 28px rgba(0,0,0,0.12)';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.transform = 'translateY(0)';
                          e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.06)';
                        }}
                      >
                        {/* Thumbnail */}
                        <div style={{ position: 'relative', overflow: 'hidden' }}>
                          <img
                            alt={course.title}
                            src={course.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600'}
                            style={{
                              width: '100%',
                              height: 180,
                              objectFit: 'cover',
                              display: 'block',
                              transition: 'transform 0.3s ease',
                            }}
                          />
                          <div style={{
                            position: 'absolute',
                            top: 10,
                            left: 10,
                          }}>
                            <Tag color={levelInfo.color} style={{ borderRadius: 6, fontWeight: 600, fontSize: 11 }}>
                              {levelInfo.label}
                            </Tag>
                          </div>
                          
                          {/* Heart Favorite Overlay */}
                          <div
                            style={{
                              position: 'absolute',
                              top: 10,
                              right: 10,
                              zIndex: 10,
                            }}
                            onClick={(e) => handleFavoriteToggle(course.id, e)}
                          >
                            <div
                              style={{
                                background: '#FFFFFF',
                                border: 'none',
                                borderRadius: '50%',
                                width: 32,
                                height: 32,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                                cursor: 'pointer',
                                transition: 'transform 0.1s ease',
                              }}
                              onMouseEnter={(e) => { e.currentTarget.style.transform = 'scale(1.1)'; }}
                              onMouseLeave={(e) => { e.currentTarget.style.transform = 'scale(1)'; }}
                              onMouseDown={(e) => { e.currentTarget.style.transform = 'scale(0.9)'; }}
                              onMouseUp={(e) => { e.currentTarget.style.transform = 'scale(1.1)'; }}
                            >
                              {favoriteIds.has(course.id) ? (
                                <HeartFilled style={{ color: '#EF4444', fontSize: 16 }} />
                              ) : (
                                <HeartOutlined style={{ color: '#9CA3AF', fontSize: 16 }} />
                              )}
                            </div>
                          </div>

                          {/* Flag Wishlist Overlay */}
                          <div
                            style={{
                              position: 'absolute',
                              top: 10,
                              right: 48,
                              zIndex: 10,
                            }}
                            onClick={(e) => handleWishlistToggle(course.id, e)}
                          >
                            <div
                              style={{
                                background: '#FFFFFF',
                                border: 'none',
                                borderRadius: '50%',
                                width: 32,
                                height: 32,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                                cursor: 'pointer',
                                transition: 'transform 0.1s ease',
                              }}
                              onMouseEnter={(e) => { e.currentTarget.style.transform = 'scale(1.1)'; }}
                              onMouseLeave={(e) => { e.currentTarget.style.transform = 'scale(1)'; }}
                              onMouseDown={(e) => { e.currentTarget.style.transform = 'scale(0.9)'; }}
                              onMouseUp={(e) => { e.currentTarget.style.transform = 'scale(1.1)'; }}
                            >
                              {wishlistIds.has(course.id) ? (
                                <FlagFilled style={{ color: '#1890ff', fontSize: 16 }} />
                              ) : (
                                <FlagOutlined style={{ color: '#9CA3AF', fontSize: 16 }} />
                              )}
                            </div>
                          </div>
                        </div>

                        {/* Content */}
                        <div style={{ padding: '16px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                          {/* Category tag */}
                          {course.category?.name && (
                            <Text style={{ color: 'var(--primary)', fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 6 }}>
                              {course.category.name}
                            </Text>
                          )}

                          {/* Title */}
                          <Text
                            strong
                            ellipsis={{ tooltip: course.title }}
                            style={{
                              color: 'var(--text-h)',
                              fontSize: 15,
                              fontWeight: 700,
                              display: 'block',
                              lineHeight: 1.4,
                              marginBottom: 8,
                            }}
                          >
                            {course.title}
                          </Text>

                          {/* Description */}
                          <Paragraph
                            ellipsis={{ rows: 2 }}
                            style={{
                              color: 'var(--text-muted)',
                              fontSize: 13,
                              margin: '0 0 12px',
                              lineHeight: 1.5,
                              flex: 1,
                            }}
                          >
                            {course.shortDescription}
                          </Paragraph>

                          {/* Instructor */}
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                            <Avatar src={course.instructor.avatarUrl} size="small" />
                            <Text
                              ellipsis
                              style={{ color: 'var(--text-muted)', fontSize: 12, flex: 1 }}
                            >
                              {course.instructor.fullName}
                            </Text>
                          </div>

                          {/* Rating and Student Count */}
                          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                              <StarFilled style={{ color: '#F59E0B', fontSize: 13 }} />
                              <Text strong style={{ color: '#92400E', fontSize: 13 }}>
                                {course.averageRating.toFixed(1)}
                              </Text>
                              <Text style={{ color: 'var(--text-muted)', fontSize: 12 }}>
                                ({course.totalReviews})
                              </Text>
                            </div>
                            <div style={{ color: 'var(--text-muted)', fontSize: 12 }}>
                              <span>👥 {course.totalStudents || 0} học viên</span>
                            </div>
                          </div>


                          {/* Price + CTA */}
                          <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            borderTop: '1px solid var(--border-color)',
                            paddingTop: 12,
                          }}>
                            <Text strong style={{ color: course.price === 0 ? 'var(--success)' : 'var(--primary)', fontSize: 16 }}>
                              {course.price === 0
                                ? 'Miễn phí'
                                : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(course.price)}
                            </Text>
                            <Button
                              type="primary"
                              size="small"
                              style={{
                                background: 'var(--primary)',
                                borderColor: 'var(--primary)',
                                borderRadius: 8,
                                fontWeight: 600,
                                fontSize: 12,
                              }}
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/courses/${course.slug}`);
                              }}
                            >
                              Xem chi tiết
                            </Button>
                          </div>
                        </div>
                      </div>
                    </Col>
                  );
                })}
              </Row>

              {/* Pagination */}
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: 36 }}>
                <Pagination
                  current={page + 1}
                  pageSize={pageSize}
                  total={total}
                  onChange={(p) => setPage(p - 1)}
                  showSizeChanger={false}
                />
              </div>
            </>
          )}
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default CourseCatalog;
