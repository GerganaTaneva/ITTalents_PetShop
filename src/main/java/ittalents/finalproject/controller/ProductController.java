package ittalents.finalproject.controller;


import com.mysql.cj.xdevapi.SessionFactory;
import ittalents.finalproject.util.exceptions.BaseException;
import ittalents.finalproject.util.exceptions.InvalidInputException;
import ittalents.finalproject.util.exceptions.ProductNotFoundException;
import ittalents.finalproject.model.pojos.Message;
import ittalents.finalproject.model.pojos.products.Product;
import ittalents.finalproject.model.pojos.products.ProductInSale;
import ittalents.finalproject.model.repos.ProductInSaleRepository;
import ittalents.finalproject.model.repos.ProductRepository;
import ittalents.finalproject.service.ProductService;
import ittalents.finalproject.util.mail.Notificator;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ittalents.finalproject.util.mail.MailUtil.NEW_PROMOTIONS_PRODUCTS_CONTENT;
import static ittalents.finalproject.util.mail.MailUtil.NEW_PROMOTIONS_SUBJECT;

@RestController
public class ProductController extends BaseController {

    @Autowired
    private ProductService productService;


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Notificator notificator;

    @Autowired
    private ImageController imageController;

    @PostMapping(value = "products/{id}/add/photo")
    public Product addPhotoToProduct(@PathVariable("id") long id,
                                     @RequestParam MultipartFile img,
                                     HttpSession session) throws BaseException, IOException {
        validateLoginAdmin(session);
        Optional<Product> product = productRepository.findById(id);
        if(!img.isEmpty() && id > 0 && product.isPresent()) {
            String imgTitle = imageController.uploadImage(img, session);
            product.get().setPhoto(imgTitle);
            productRepository.save(product.get());
            return product.get();
        }
        else {
            throw new ProductNotFoundException("No product found with that id. Cannot set image.");
        }
    }

    @GetMapping(value = "products/{id}/photo", produces = "image/png")
    public @ResponseBody byte[] showProductImage(@PathVariable("id") long id) throws IOException, BaseException{
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()) {
            String image = product.get().getPhoto();
            if(!image.equals("no photo")) {
                return imageController.downloadImage(image);
            }
            else {
                throw new ProductNotFoundException("No photo for this product.");
            }
        }
        else {
            throw new ProductNotFoundException("No product found with that id. Cannot show image.");
        }
    }



    @GetMapping(value = "products/filter")
    public List<Product> getAllProductsFiltered(@RequestParam(name = "sortBy", required = false) String sortBy,
                                                @RequestParam(name = "minPrice", required = false) Double minPrice,
                                                @RequestParam(name = "maxPrice", required = false) Double maxPrice,
                                                @RequestParam(name = "category", required = false) String category,
                                                @RequestParam(name = "name", required = false) String name) {
        return productRepository.findAll()
                .stream()
                .filter(product -> minPrice == null ||
                        product.getPrice() >= minPrice )
                .filter(product -> maxPrice == null ||
                        product.getPrice() <= maxPrice)
                .filter(product -> category == null ||
                        product.getCategory().equals(category))
                .filter(product -> name == null ||
                        product.getName().contains(name))
                .sorted((c1, c2) -> {
                    if(sortBy == null) return 1;
                    switch (sortBy) {
                        case "category" : return c1.getCategory().compareTo(c2.getCategory());
                        case "description" : return c1.getDescription().compareTo(c2.getDescription());
                        case "manufacturer" : return c1.getManifacturer().compareTo(c2.getManifacturer());
                        case "name" : return c1.getName().compareTo(c2.getName());
                        case "price" : return Double.compare(c1.getPrice(),c2.getPrice());
                        default: return 1;
                    }
                })
                .map(product -> new Product(product.getId(),
                                            product.getName(),
                                            product.getCategory(),
                                            product.getPrice(),
                                            product.getQuantity(),
                                            product.getManifacturer(),
                                            product.getDescription(),
                                            product.getPhoto()))
                .collect(Collectors.toList());
    }



    @GetMapping(value = "/products/search/{name}")
    public List<Product> findProducts(@PathVariable("name") String name) throws BaseException{
        List<Product> products = productRepository.search(name);
        if(products.isEmpty()) {
            throw new ProductNotFoundException("No products found containing that name.");
        }
        return products;
    }

    @GetMapping(value = "/products/sort/price")
    public List<Product> sortByPrice() throws BaseException{
        List<Product> products = productRepository.sortByPrice();
        if(products.isEmpty()) {
            throw new ProductNotFoundException("No products found.");
        }
        return products;
    }

    @GetMapping(value = "/products")
    public List<Product> getAll(HttpSession session) throws BaseException {
        validateLogin(session);
        List<Product> allProducts = productRepository.findAll();
        if(allProducts.isEmpty()) {
            throw new ProductNotFoundException("No products found.");
        }
        return allProducts;
    }



    @GetMapping(value = "/products/{id}")
    public Product getById(@PathVariable("id") long id, HttpSession session) throws BaseException{
        validateLogin(session);
        Optional<Product> obj = productRepository.findById(id);
        if(obj.isPresent()) {
            return obj.get();
        }
        else {
            throw new ProductNotFoundException("Product not found with that id.");
        }
    }

    @PostMapping(value = "/products/byName")
    public Optional<Product> showProductByName(@RequestParam("name") String name) throws BaseException{
        Optional<Product> product = productRepository.findByName(name);
        if(product.isPresent()) {
            return product;
        }
        else {
            throw new ProductNotFoundException("Product not found with that name.");
        }
    }


//    @PostMapping(value = "/products/filter/category")
//    public List<Product> filterByPrice(@RequestParam("category") String category) throws BaseException {
//        List<Product> products = productRepository.findAllByCategoryOrderByPrice(category);
//        if(products.isEmpty()) {
//            throw new ProductNotFoundException("No products found out of that category.");
//        }
//        return products;
//    }


    @PostMapping(value = "/products/add")
    public Product add(@RequestBody Product product, HttpSession session) throws BaseException {
        validateLoginAdmin(session);
        validateProductInput(product);
        validateProductByName(product);
        productRepository.save(product);
        return product;
    }

    @PutMapping(value = "/products/update")
    public Product update(@RequestBody Product product, HttpSession session) throws BaseException{
        validateLoginAdmin(session);
        validateProductInput(product);
        if (!productRepository.findById(product.getId()).isPresent()) {
            throw new InvalidInputException("There is not product with this id in the database.");
        }
        productRepository.save(product);
        return product;
    }

    //todo to update in sale table and review
    //1 to many
    @DeleteMapping(value = "/products/remove/{id}")
    public Object remove(@PathVariable("id") long id, HttpSession session) throws BaseException {
        validateLoginAdmin(session);
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()) {
            productRepository.delete(product.get());
            return new Message(product.get().getName() + " was successfully removed from the database.",
                    LocalDateTime.now(), HttpStatus.OK.value());
        }
        else {
            throw new InvalidInputException("The product is not present in the database.");
        }
    }

    @Autowired
    private ProductInSaleRepository productInSaleRepository;

    @PostMapping(value = "/products/sale/add")
    public ProductInSale addProductToSale(@RequestBody ProductInSale productInSale, HttpSession session) throws BaseException{
        validateLoginAdmin(session);

        if(productRepository.findById(productInSale.getProductId()).isPresent()) {
            if(productInSale.getStartDate().compareTo(productInSale.getEndDate()) > 0) {
                throw new InvalidInputException("The start date can not be after the end date.");
            }
            productInSaleRepository.save(productInSale);
            notificator.sendNews(NEW_PROMOTIONS_SUBJECT, NEW_PROMOTIONS_PRODUCTS_CONTENT);
            return productInSale;
        }
        else {
            throw new InvalidInputException("There is no product with that id in the main table.");
        }
    }


    private void validateProductInput(Product product)throws BaseException {
        if(product.getName() == null || product.getCategory() == null || product.getPrice() < 0
                || product.getQuantity() < 0 || product.getManifacturer() == null
                || product.getDescription() == null ){
            throw new InvalidInputException("Invalid input for the product input.");
        }
    }
    private void validateProductByName(Product product) throws BaseException {
        if (getProductByName(product.getName()) != null) {
            throw new InvalidInputException("Product with that name already exists.");
        }
    }

    private Product getProductByName(String name) {
        Optional<Product> product = productRepository.findByName(name);
        if (product.isPresent()) {
            return product.get();
        }
        else {
            return null;
        }
    }
}
